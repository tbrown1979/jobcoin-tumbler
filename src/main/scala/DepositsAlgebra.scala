package com.tbrown.jobcoin

import cats.data.NonEmptyList
import cats.effect.Sync
import cats.implicits._

trait DepositsAlgebra[F[_]] {
  def createDeposits(mixId: MixId, total: Amount, addresses: NonEmptyList[Address]): F[Unit]
  def getDeposits: F[List[TumbledDeposit]]
  def completeDeposit(depositId: TumbledDepositId): F[Unit]
}

class InMemoryDepositsAlgebra[F[_]](implicit S: Sync[F]) extends DepositsAlgebra[F] {
  var deposits: List[TumbledDeposit] = Nil

  //this is a super simple implementation
  //it divides the total by the amount of addresses and creates a deposit for that amount for each address
  //this could be randomized,
  def createDeposits(mixId: MixId, total: Amount, addresses: NonEmptyList[Address]): F[Unit] = S.delay {
    val bd = BigDecimal(total.value)
    val depositAmount = Amount((bd / addresses.size).toString)//pretty insecure distribution of amounts..
    def id = TumbledDepositId(java.util.UUID.randomUUID().toString)

    deposits = deposits ++ addresses.map { addr =>
      TumbledDeposit(id, mixId, depositAmount, addr)
    }.toList
  }

  def getDeposits: F[List[TumbledDeposit]] = S.delay(deposits)

  def completeDeposit(depositId: TumbledDepositId): F[Unit] = S.delay {
    deposits = deposits.filterNot(_.id == depositId)
  }.void
}