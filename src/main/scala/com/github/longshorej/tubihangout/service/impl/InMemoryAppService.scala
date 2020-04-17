package com.github.longshorej.tubihangout.service.impl

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{BroadcastHub, Keep, MergeHub, Sink, Source}
import com.github.longshorej.tubihangout.http.HangoutCreated
import com.github.longshorej.tubihangout.model.{Hangout, HangoutEvent, HangoutVideo}
import com.github.longshorej.tubihangout.service.{AppService, Command}

import scala.concurrent.{Future, Promise}

object InMemoryAppService {
  private case class CommandWithPromise[A](command: Command[A]) {
    val result: Promise[A] = Promise[A]
  }

  private case class AppState(hangouts: Map[UUID, Hangout])

  private object AppState {
    // @TODO have Empty actually be empty once initial dev is done
    val Empty: AppState = AppState(
      hangouts = Map(
        UUID.fromString("ac67a7e3-8f1f-4dd5-bdf5-8ede3bb78cba") -> Hangout(
          UUID.fromString("ac67a7e3-8f1f-4dd5-bdf5-8ede3bb78cba"),
          "Test",
          None
        )
      )
    )
  }
}

class InMemoryAppService()(implicit system: ActorSystem) extends AppService {
  import InMemoryAppService._

  private val commandSink = MergeHub
    .source[CommandWithPromise[_]]
    .fold(AppState.Empty) {
      case (state, container) =>
        val (newState, result, events) = handle(state, container.command)

        container.result.success(result)

        Source(events).runWith(hangoutEventSink)

        newState
    }
    .to(Sink.ignore)
    .run()

  // @TODO config
  private val (hangoutEventSink, hangoutEventSource) = MergeHub
    .source[HangoutEvent](perProducerBufferSize = 16)
    .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both)
    .run()

  def execute[A](command: Command[A]): Future[A] = {
    // @TODO default timeout?

    val commandWithPromise = CommandWithPromise(command)

    Source.single(commandWithPromise).runWith(commandSink)

    commandWithPromise.result.future
  }

  def subscribe(hangoutId: UUID): Source[HangoutEvent, NotUsed] =
    hangoutEventSource.filter(_.hangoutId == hangoutId)

  private def handle[A](state: AppState, command: Command[A]): (AppState, A, List[HangoutEvent]) =
    command match {
      case Command.CreateHangout(name) =>
        val id = UUID.randomUUID()

        (
          state.copy(hangouts = state.hangouts.updated(id, Hangout(id, name, video = None))),
          id,
          Nil
        )

      case Command.UpdateVideoPosition(hangoutId, id, position, paused) =>
        state.hangouts.get(hangoutId) match {
          case Some(hangout) =>
            (
              state.copy(
                hangouts = state.hangouts.updated(
                  hangoutId,
                  hangout.copy(
                    video = Some(HangoutVideo(id, paused, position))
                  )
                )
              ),
              (),
              List(HangoutEvent.VideoPositionUpdated(hangoutId, id, position, paused))
            )

          case None =>
            (state, (), Nil)
        }
    }
}
