package com.github.longshorej.tubihangout.http

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling
import akka.http.scaladsl.model.{HttpMethods, HttpResponse, StatusCodes, headers}
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.scaladsl.{Keep, Sink}
import com.github.longshorej.tubihangout.model.HangoutEvent
import com.github.longshorej.tubihangout.service.{AppService, Command}
import spray.json._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Routes extends Directives with HttpApiJsonSupport with EventStreamMarshalling {
  def apply(service: AppService)(implicit ec: ExecutionContext, system: ActorSystem): Route = concat(
    (get & path("ping")) (complete("pong!")),

    (get & path("hangouts" / JavaUUID / "join")) { hangoutId =>
      onSuccess(
        service
          .subscribe(hangoutId)
          .collect {
            case HangoutEvent.VideoPositionUpdated(_, tubiId, _, _) =>
              redirect(s"https://tubitv.com/movies/$tubiId?hangoutId=$hangoutId", StatusCodes.TemporaryRedirect)
          }
          .toMat(Sink.head)(Keep.right)
          .run()
      )(identity)
    },

    pathPrefix("api") {
      Route.seal(
        respondWithHeaders(headers.`Cache-Control`(headers.CacheDirectives.`no-transform`),
          headers.`Access-Control-Allow-Origin`.*)(concat(
          options {
            complete(
              HttpResponse(StatusCodes.OK)
                .withHeaders(
                  headers.`Access-Control-Allow-Methods`(
                    HttpMethods.OPTIONS,
                    HttpMethods.POST,
                    HttpMethods.PUT,
                    HttpMethods.GET,
                    HttpMethods.DELETE
                  ),

                  headers.`Access-Control-Allow-Headers`(
                    "Authorization",
                    "Content-Type",
                    "X-Requested-With"
                  )
                )
            )
          },

          pathPrefix("hangouts") {
            concat(
              (post & pathEnd) {
                entity(as[CreateHangout]) { createHangout =>
                  complete(
                    service
                      .execute(Command.CreateHangout(createHangout.name))
                      .map(HangoutCreated.apply)
                  )
                }
              },

              (post & path(JavaUUID / "commands" / "update-video-position")) { hangoutId =>
                entity(as[UpdateVideoPosition]) { updateVideoPosition =>
                  complete(
                    service
                      .execute(
                        Command.UpdateVideoPosition(
                          hangoutId,
                          updateVideoPosition.id,
                          updateVideoPosition.position,
                          updateVideoPosition.paused
                        )
                      )
                      .map(_ => StatusCodes.OK)
                  )
                }
              },

              (get & path(JavaUUID / "events")) { hangoutId =>
                complete(
                  service
                    .subscribe(hangoutId)
                    .map(event => ServerSentEvent(event.toJson.compactPrint))
                    .keepAlive(10.seconds, () => ServerSentEvent.heartbeat)
                )
              }
            )
          }
        ))
      )
    }
  )
}
