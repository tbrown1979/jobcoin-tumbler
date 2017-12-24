package com.tbrown.jobcoin

import java.time.Instant

import cats.Functor
import cats.data.NonEmptyList
import cats.effect.Sync
import cats.implicits._
//import io.circe.java8.time._

case class MixId(value: String)
case class Mix(id: MixId, depositAddress: DepositAddress, addresses: NonEmptyList[Address], createdAt: Instant)
case class DepositAddress(value: String)

trait MixAlgebra[F[_]] {
  def addMix(address: NonEmptyList[Address]): F[DepositAddress]
  def getMixes: F[List[Mix]]
}

class InMemoryMixInterpreter[F[_]: Functor](implicit S: Sync[F]) extends MixAlgebra[F] {
  var mixes: List[Mix] = Nil

  def addMix(addresses: NonEmptyList[Address]): F[DepositAddress] = {
    val mixId = MixId(java.util.UUID.randomUUID().toString)
    val depositAddress = DepositAddress(java.util.UUID.randomUUID().toString)
    S.delay(mixes = Mix(mixId, depositAddress, addresses, Instant.now()) :: mixes).map { _ => //abstract out time provider?
      DepositAddress(java.util.UUID.randomUUID().toString)
    }
  }

  def getMixes: F[List[Mix]] = S.delay(mixes)
}
