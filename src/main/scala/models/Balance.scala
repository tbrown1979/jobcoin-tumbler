package com.tbrown.jobcoin

import cats.effect.Effect

import io.circe._

import org.http4s.circe._
import org.http4s.EntityDecoder

case class Balance(value: String) extends AnyVal
object Balance {
  import io.circe.generic.extras.semiauto._

  implicit val decoderTransfer: Decoder[Balance] = deriveUnwrappedDecoder
  implicit val encoderTransfer: Encoder[Balance] = deriveUnwrappedEncoder

  implicit def addressInfoEntityDecoder[F[_]](implicit F: Effect[F]): EntityDecoder[F, Balance] =
    jsonOf[F, Balance]
}
