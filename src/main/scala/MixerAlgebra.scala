package com.tbrown.jobcoin

import cats.data.NonEmptyList
import cats.effect.Sync
import cats.Functor
import cats.implicits._

trait MixerAlgebra[F[_]] {
  def addMix(addresses: NonEmptyList[Address], depositAddress: DepositAddress): F[Unit]
  def getMixes: F[List[Mix]]
  def removeMix(mixId: MixId): F[Unit]
}

class InMemoryMixerInterpreter[F[_]: Functor](implicit S: Sync[F]) extends MixerAlgebra[F] {
  var mixes: List[Mix] = Nil //these never expire and will eventually fill memory

  def addMix(addresses: NonEmptyList[Address], depositAddress: DepositAddress): F[Unit] = {
    val mixId = MixId(java.util.UUID.randomUUID().toString)

    //abstract out time provider?
    S.delay(mixes = Mix.newMix(mixId, depositAddress, addresses) :: mixes).void
  }

  def getMixes: F[List[Mix]] = S.delay(mixes)

  def removeMix(mixId: MixId): F[Unit] = S.delay {
    mixes.filterNot(_.id == mixId)
  }.void
}