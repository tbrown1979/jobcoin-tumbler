package com.tbrown.jobcoin

import java.time.Instant

import cats.Monad
import cats.data.NonEmptyList
import cats.effect.Sync
import cats.implicits._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
//import io.circe.java8.time._


sealed trait MixStatus
case object Initiated extends MixStatus
case object InHouse extends MixStatus

case class MixId(value: String)
case class Mix(id: MixId, depositAddress: DepositAddress, status: MixStatus, addresses: NonEmptyList[Address], createdAt: Instant)
object Mix {
  def newMix(id: MixId, depositAddress: DepositAddress, addresses: NonEmptyList[Address]) =
    Mix(id, depositAddress, Initiated, addresses, Instant.now())
}

case class DepositAddress(value: String Refined NonEmpty)
//object

trait MixerService[F[_]] {
  def createMix(address: NonEmptyList[Address]): F[DepositAddress]
//  def getMixes: F[List[Mix]]
}

class DefaultMixerService[F[_]: Monad: Sync](
  mixer: MixerAlgebra[F], jobCoin: JobCoinAlgebra[F]) extends MixerService[F] {

  def createMix(addresses: NonEmptyList[Address]): F[DepositAddress] =
    for {
      depositAddress <- jobCoin.randomDepositAddress
      _ <- mixer.addMix(addresses, depositAddress)
    } yield depositAddress
}
