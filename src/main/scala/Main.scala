package com.tbrown.jobcoin

import cats.effect._
import cats.effect.IO

import eu.timepit.refined._

import fs2._

import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.{ExitCode, StreamApp}

import org.log4s.getLogger

import scala.concurrent.ExecutionContext

object Server extends StreamApp[IO] {
  private[this] val logger = getLogger

  override def stream(args: List[String], shutdown: IO[Unit]): Stream[IO, ExitCode] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    createStream[IO](args, shutdown)
  }

  def createStream[F[_]](args: List[String], shutdown: F[Unit])(
    implicit E: Effect[F], EC: ExecutionContext): Stream[F, ExitCode] =
    for {
      _       <- Stream.eval(E.delay(logger.debug("Starting app")))
      mixer   = new InMemoryMixerInterpreter[F]()
      jobCoin = new JobCoinInterpreter[F]()
      poller  = new TransactionPollingInterpreter(Address(refineMV("House")), jobCoin, mixer)

      exitCode      <- BlazeBuilder[F]
        .bindHttp(8080, "localhost")
        .mountService(new MixerEndpoints(new DefaultMixerService(mixer, jobCoin)).mixerEndpoints, "/")
        .serve
        .concurrently(poller.poll(10))
    } yield exitCode
}