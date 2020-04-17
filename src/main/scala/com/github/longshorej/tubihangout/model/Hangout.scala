package com.github.longshorej.tubihangout.model

import java.util.UUID

/**
 * Represents a hangout which has a name and an optional video.
 */
case class Hangout(id: UUID, name: String, video: Option[HangoutVideo])