package com.github.longshorej.tubihangout.service

import java.util.UUID

sealed trait Command[A]

object Command {
  /**
   * Create a new hangout with the supplied name, which is strictly a
   * label and doesn't have any constraints, uniqueness, etc.
   */
  case class CreateHangout(name: String) extends Command[UUID]

  /**
   * Update the video's progress to be at the supplied time in milliseconds.
   */
  case class UpdateVideoPosition(hangoutId: UUID,
                                 id: Long,
                                 position: Long,
                                 paused: Boolean) extends Command[Unit]
}
