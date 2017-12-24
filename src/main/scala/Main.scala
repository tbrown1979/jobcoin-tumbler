package com.tbrown.jobcoin

import cats.effect.IO

import fs2._

//import org.http4s.client.blaze.PooledHttp1Client
import org.http4s.util.{ExitCode, StreamApp}

import cats.effect._
//import cats.implicits._
import fs2.Stream
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp
import org.http4s.util.ExitCode

object Server extends StreamApp[IO] {

  override def stream(args: List[String], shutdown: IO[Unit]): Stream[IO, ExitCode] =
    createStream[IO](args, shutdown)

  def createStream[F[_]](args: List[String], shutdown: F[Unit])(
    implicit E: Effect[F]): Stream[F, ExitCode] =
    for {
      _ <- Stream.eval(E.delay(1))
//      conf          <- Stream.eval(PetStoreConfig.load[F])
//      xa            <- Stream.eval(DatabaseConfig.dbTransactor(conf.db))
//      _             <- Stream.eval(DatabaseConfig.initializeDb(conf.db, xa))
//      petRepo       =  DoobiePetRepositoryInterpreter[F](xa)
//      orderRepo     =  DoobieOrderRepositoryInterpreter[F](xa)
//      petValidation =  PetValidationInterpreter[F](petRepo)
//      petService    =  PetService[F](petRepo, petValidation)
//      orderService  =  OrderService[F](orderRepo)
      exitCode      <- BlazeBuilder[F]
        .bindHttp(8080, "localhost")
        .mountService(new MixerEndpoints(new InMemoryMixInterpreter).mixerEndpoints, "/")
//        .mountService(OrderEndpoints.endpoints[F](orderService), "/")
        .serve
    } yield exitCode
}