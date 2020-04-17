package com.github.longshorej.tubihangout.model

import java.util.UUID

sealed trait HangoutEvent {
  def hangoutId: UUID
}

object HangoutEvent {
  case class VideoPositionUpdated(hangoutId: UUID, id: Long, position: Long, paused: Boolean) extends HangoutEvent
}
