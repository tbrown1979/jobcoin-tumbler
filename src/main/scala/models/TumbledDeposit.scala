package com.tbrown.jobcoin

case class TumbledDepositId(value: String)
case class TumbledDeposit(id: TumbledDepositId, mixId: MixId, amount: Amount, address: Address)

