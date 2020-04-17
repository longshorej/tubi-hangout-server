package com.github.longshorej.tubihangout.http

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.github.longshorej.tubihangout.model.HangoutEvent
import spray.json._

trait HttpApiJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val uuidFormat: RootJsonFormat[UUID] =
    new RootJsonFormat[UUID] {
      override def read(json: JsValue): UUID =
        UUID.fromString(json.convertTo[String])

      override def write(obj: UUID): JsValue =
        JsString(obj.toString)
    }

  implicit val createHangoutFormat: RootJsonFormat[CreateHangout] =
    jsonFormat(CreateHangout, "name")

  implicit val hangoutCreatedFormat: RootJsonFormat[HangoutCreated] =
    jsonFormat(HangoutCreated, "id")

  implicit val updateVideoPositionFormat: RootJsonFormat[UpdateVideoPosition] =
    jsonFormat(UpdateVideoPosition, "id", "position", "paused")

 implicit val eventFormat: RootJsonWriter[HangoutEvent] =
   {
     case HangoutEvent.VideoPositionUpdated(hangoutId, id, position, paused) =>
       JsObject(
         "type"      -> JsString("VideoPositionUpdated"),
         "hangoutId" -> hangoutId.toJson,
         "id"        -> JsNumber(id),
         "position"  -> JsNumber(position),
         "paused"    -> JsBoolean(paused)
       )
   }
}
