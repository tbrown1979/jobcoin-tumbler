package com.tbrown.jobcoin

import cats.effect.Effect
import cats.Functor
import cats.implicits._

import fs2._

import org.log4s.getLogger

import scala.concurrent.duration._
import scala.language.postfixOps

import scala.math.max

trait TransactionPollingAlgebra[F[_]] {
  def poll(interval: Int): Stream[F, Unit]
}

class TransactionPollingInterpreter[F[_]: Functor](houseAccount: Address, jobCoin: JobCoinAlgebra[F], mixer: MixerAlgebra[F])(implicit F: Effect[F]) {
  import scala.concurrent.ExecutionContext.Implicits.global

  private[this] val logger = getLogger
  val schedulerExecutor = new java.util.concurrent.ScheduledThreadPoolExecutor(1)

  implicit val scheduler = Scheduler.fromScheduledExecutorService(schedulerExecutor)

  def poll(interval: Int): Stream[F, Unit] =
    scheduler.awakeEvery[F](interval seconds)
      .flatMap(_ => moveCoinsToHouse)
      .onError { t =>
        logger.error(t)("error moving jobcoins to house account")
        Stream.emit(())
      }
      .repeat //start it back up


  def moveCoinsToHouse: Stream[F, Unit] = {
    for {
      transactions   <- Stream.eval(jobCoin.getTransactions)
      mixes          <- Stream.eval(mixer.getMixes)
      mixesToPerform =  mixes.filter(m => transactions.exists(_.toAddress.value == m.depositAddress.value))
      _ <- Stream.eval(F.delay(logger.debug(s"Mixes we've identified: $mixesToPerform")))
      _            <- Stream.emits(mixesToPerform).covary[F].map { mix =>
        Stream.eval {
          logger.debug(s"Getting address info for mix: $mix")
          jobCoin.getAddressInfo(Address(mix.depositAddress.value)).flatMap { info =>
            jobCoin.performTransaction(
              FromAddress(mix.depositAddress.value),
              ToAddress(houseAccount.value),
              Amount(info.balance.value))
          }
        }
      }.join(max(Runtime.getRuntime.availableProcessors, 2))
    } yield ()
  }
}