package com.tbrown.jobcoin

import cats.Monad
import cats.data.NonEmptyList
import cats.effect.Sync
import cats.implicits._

trait MixerService[F[_]] {
  def createMix(address: NonEmptyList[Address]): F[DepositAddress]
}

class DefaultMixerService[F[_]: Monad: Sync](
  mixer: MixerAlgebra[F], jobCoin: JobCoinAlgebra[F]) extends MixerService[F] {

  def createMix(addresses: NonEmptyList[Address]): F[DepositAddress] =
    for {
      depositAddress <- jobCoin.randomDepositAddress
      _ <- mixer.addMix(addresses, depositAddress)
    } yield depositAddress
}
