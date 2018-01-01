package com.tbrown.jobcoin

import java.time.Instant

import cats.data.NonEmptyList

sealed trait MixStatus
case object Initiated extends MixStatus
case object InHouse extends MixStatus
case object Completed extends MixStatus //not used currently

case class MixId(value: String)

case class Mix(
  id: MixId,
  depositAddress: DepositAddress,
  status: MixStatus,
  addresses: NonEmptyList[Address],
  createdAt: Instant)

object Mix {
  def newMix(id: MixId, depositAddress: DepositAddress, addresses: NonEmptyList[Address]) =
    Mix(id, depositAddress, Initiated, addresses, Instant.now())
}