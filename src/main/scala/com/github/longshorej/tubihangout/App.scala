package com.github.longshorej.tubihangout

import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.http.scaladsl.Http
import com.github.longshorej.tubihangout.service.AppService
import com.github.longshorej.tubihangout.service.impl.InMemoryAppService

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object CoordinatedShutdownReasons {
  case class HttpBindFailureReason(underlying: Throwable) extends CoordinatedShutdown.Reason
}

object App {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("my-system")
    implicit val ec: ExecutionContext = system.dispatcher

    val coordinatedShutdown: CoordinatedShutdown = CoordinatedShutdown(system)
    val appService: AppService = new InMemoryAppService()

    Http()
      .bindAndHandle(http.Routes(appService), "0.0.0.0", 8080)
      .onComplete {
        case Success(socket) =>
          system.log.info(
            "HTTP server bound to {}:{}",
            socket.localAddress.getHostName,
            socket.localAddress.getPort
          )

        case Failure(failure) =>
          coordinatedShutdown.run(CoordinatedShutdownReasons.HttpBindFailureReason(failure))
      }
  }
}
