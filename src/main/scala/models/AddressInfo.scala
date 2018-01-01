package com.tbrown.jobcoin

import cats.effect.Effect

import io.circe._
import io.circe.generic.semiauto._

import org.http4s.circe._
import org.http4s.EntityDecoder

case class AddressInfo(balance: Balance, transactions: List[Transaction])

object AddressInfo {
  implicit val decoderTransfer: Decoder[AddressInfo] = deriveDecoder
  implicit val encoderTransfer: Encoder[AddressInfo] = deriveEncoder

  implicit def addressInfoEntityDecoder[F[_]](implicit F: Effect[F]): EntityDecoder[F, AddressInfo] =
    jsonOf[F, AddressInfo]
}
