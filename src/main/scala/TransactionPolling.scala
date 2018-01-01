package com.tbrown.jobcoin

import cats.effect.Effect
import cats.Functor
import cats.implicits._
import fs2._
import org.log4s.getLogger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.math.max

trait TransactionPollingAlgebra[F[_]] {
  def poll(interval: Int): Stream[F, Unit]
}

class TransactionPollingInterpreter[F[_]: Functor](
  houseAccount: HouseAccount,
  jobCoin: JobCoinAlgebra[F],
  mixer: MixerAlgebra[F],
  depAlg: DepositsAlgebra[F])(implicit F: Effect[F], EC: ExecutionContext) {
  private[this] val logger = getLogger

  val schedulerExecutor = new java.util.concurrent.ScheduledThreadPoolExecutor(1)
  implicit val scheduler = Scheduler.fromScheduledExecutorService(schedulerExecutor)

  def poll(interval: Int): Stream[F, Unit] =
    scheduler.awakeEvery[F](interval seconds)
      .evalMap(_ => jobCoin.getTransactions)
      .through(initiatedMixes)
      .to(moveToHouse)
      .onError { t =>
        logger.error(t)("error moving jobcoins to house account")
        Stream.emit(())
      }
      .repeat //start it back up

  def initiatedMixes: Pipe[F, List[Transaction], Mix] =
    _.flatMap { transactions =>
      Stream.eval(mixer.getMixes)
        .flatMap(Stream.emits(_))
        .filter(mix => mix.status == Initiated && transactions.exists(_.toAddress.value == mix.depositAddress.depositAddress))
    }

  def moveToHouse: Sink[F, Mix] =
    _.map { mix =>
      Stream.eval {
        for {
          info <- jobCoin.getAddressInfo(Address(mix.depositAddress.depositAddress))
          from =  FromAddress(mix.depositAddress.depositAddress)
          to   =  ToAddress(houseAccount.value)
          amt  =  Amount(info.balance.value)
          _    <- jobCoin.performTransaction(from, to, amt)
          _    <- mixer.updateMix(mix.id, InHouse)
          _    <- depAlg.createDeposits(mix.id, Amount(info.balance.value), mix.addresses)
        } yield ()
      }.onError { t => //could make error handling more specific, but this is easier for now
        logger.error(t)("Failed while moving the deposited amount to the house account")
        Stream.emit(()) //it's okay to fail while depositing to the house. We'll try again.
      }
    }.join(max(Runtime.getRuntime.availableProcessors, 2))
}
