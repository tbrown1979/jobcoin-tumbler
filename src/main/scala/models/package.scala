package com.tbrown.jobcoin

import cats.effect.Effect

import io.circe._
import io.circe.generic.semiauto._
import io.circe.Decoder._

import org.http4s._
import org.http4s.circe._

case class Transaction(toAddress: Address, fromAddress: Option[Address], amount: String)

object Transaction {
  implicit val decoderTransaction: Decoder[Transaction] = deriveDecoder
  implicit val encoderTransaction: Encoder[Transaction] = deriveEncoder

  implicit def transactionsEntityDecoder[F[_]](implicit F: Effect[F]): EntityDecoder[F, Transaction] =
    jsonOf[F, Transaction]

  implicit def listTransactionsEntityDecoder[F[_]](implicit F: Effect[F]): EntityDecoder[F, List[Transaction]] =
    jsonOf[F, List[Transaction]]
}