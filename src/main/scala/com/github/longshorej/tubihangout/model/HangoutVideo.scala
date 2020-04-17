package com.github.longshorej.tubihangout.model

/**
 * Represents a video for a hangout, consisting of its id (Tubi-assigned),
 * whether the video is currently paused, and the more recent playback
 * position in milliseconds.
 */
case class HangoutVideo(id: Long, paused: Boolean, position: Long)