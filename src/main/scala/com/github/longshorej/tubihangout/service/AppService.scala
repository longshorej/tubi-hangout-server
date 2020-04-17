package com.github.longshorej.tubihangout.service

import java.util.UUID

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.github.longshorej.tubihangout.model.HangoutEvent

import scala.concurrent.Future

trait AppService {
  def execute[A](command: Command[A]): Future[A]
  def subscribe(hangoutId: UUID): Source[HangoutEvent, NotUsed]
}
