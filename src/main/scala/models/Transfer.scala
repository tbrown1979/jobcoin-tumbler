package com.tbrown.jobcoin

import cats.effect.Effect
import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import io.circe._
import io.circe.generic.semiauto._
import org.http4s.circe._
import org.http4s.EntityDecoder

import scala.util.control.NoStackTrace

case class FromAddress(value: String Refined NonEmpty)

object FromAddress {
  implicit val decoder: Decoder[FromAddress] = Decoder.decodeString.emap(refineV[NonEmpty](_).map(FromAddress(_)))
  implicit val encoder: Encoder[FromAddress] = Encoder.encodeString.contramap(_.value.value)
}
case class ToAddress(value: String Refined NonEmpty)

object ToAddress {
  implicit val decoder: Decoder[ToAddress] = Decoder.decodeString.emap(refineV[NonEmpty](_).map(ToAddress(_)))
  implicit val encoder: Encoder[ToAddress] = Encoder.encodeString.contramap(_.value.value)
}

case class Transfer(fromAddress: FromAddress, toAddress: ToAddress, amount: Amount)

object Transfer {
  implicit val decoderTransfer: Decoder[Transfer] = deriveDecoder
  implicit val encoderTransfer: Encoder[Transfer] = deriveEncoder

  implicit def transferEntityDecoder[F[_]](implicit F: Effect[F]): EntityDecoder[F, Transfer] =
    jsonOf[F, Transfer]
}

case object InsufficientFunds extends RuntimeException with NoStackTrace
case object TransferFailed extends RuntimeException with NoStackTrace
