package com.tbrown.jobcoin

import fs2._
import cats.effect.Effect
import cats.implicits._

import scala.math.max
import org.log4s.getLogger

import scala.concurrent.duration._
import scala.language.postfixOps

trait MixDistributorAlgebra[F[_]] {
  def distributeMixes: Stream[F, Unit]
}

//specific type for HouseAccount?
//take in EC as a param?
class MixDistributorInterpreter[F[_]](
  houseAccount: HouseAccount, depositAlg: DepositsAlgebra[F], jobCoin: JobCoinAlgebra[F])(implicit F: Effect[F]) {
  import scala.concurrent.ExecutionContext.Implicits.global

  private[this] val logger = getLogger

  val schedulerExecutor = new java.util.concurrent.ScheduledThreadPoolExecutor(1)
  implicit val scheduler = Scheduler.fromScheduledExecutorService(schedulerExecutor)

  //this makes 1 deposit per Mix every interval. This interval here could be randomized to produce less uniform dispersion
  def start(interval: Int): Stream[F, Unit] =
    scheduler.awakeEvery[F](interval seconds)
      .evalMap(_ => depositAlg.getDeposits)
      .map(deps => deps.groupBy(_.mixId).values.toList.flatMap(_.headOption))
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
          //don't make the transfer _unless_ we successfully marked the deposit as completed
          //if we switched these then it is possible that we could send out more money than we wanted to
          _ <- jobCoin.performTransaction(FromAddress(houseAccount.value), ToAddress(dep.address.value), dep.amount)
        } yield ()
      }.onError { t =>
        logger.error(t)(s"Failed making deposit $dep")
        Stream.emit(())
      }
    }.join(max(Runtime.getRuntime.availableProcessors, 2))
}