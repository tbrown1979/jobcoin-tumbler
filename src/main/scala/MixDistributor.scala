package com.tbrown.jobcoin

import fs2._
import cats.effect.Effect
import cats.implicits._

import scala.math.max
import org.log4s.getLogger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

trait MixDistributorAlgebra[F[_]] {
  def distributeMixes: Stream[F, Unit]
}

class MixDistributorInterpreter[F[_]](
  houseAccount: HouseAccount,
  depositAlg: DepositsAlgebra[F],
  jobCoin: JobCoinAlgebra[F])(implicit F: Effect[F], EC: ExecutionContext) {
  private[this] val logger = getLogger

  val schedulerExecutor = new java.util.concurrent.ScheduledThreadPoolExecutor(1)
  implicit val scheduler = Scheduler.fromScheduledExecutorService(schedulerExecutor)

  //this makes 1 deposit per Mix per interval. This interval could be randomized to produce less uniform dispersion
  def start(interval: Int): Stream[F, Unit] =
    scheduler.awakeEvery[F](interval seconds)
      .evalMap(_ => depositAlg.getDeposits)
      .map(_.groupBy(_.mixId).values.toList.flatMap(_.headOption))
      .flatMap(Stream.emits(_))
      .to(makeDeposit)
      .onError { t =>
        logger.error(t)("Encountered an error while trying to disperse deposits. Restarting the process..")
        Stream.emit(())
      }
      .repeat

  val makeDeposit: Sink[F, TumbledDeposit] =
    _.map { dep =>
      Stream.eval {
        for {
          _ <- depositAlg.completeDeposit(dep.id)
          _ <- jobCoin.performTransaction(FromAddress(houseAccount.value), ToAddress(dep.address.value), dep.amount)
        } yield ()
      }.onError { t => //again, this is just an easy way to recover from the failure for now
        logger.error(t)(s"Failed making deposit $dep")
        Stream.emit(())
      }
    }.join(max(Runtime.getRuntime.availableProcessors, 2))
}