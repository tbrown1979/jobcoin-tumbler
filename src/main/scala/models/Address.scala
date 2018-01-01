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

case class HouseAccount(value: String Refined NonEmpty)

//no semi-auto deriviation of enc/dec with shapeless-tagged refinements. Resorting to some boilerplate
//https://github.com/circe/circe/issues/220
//also can't derive enc/dec for value classes containing refined types
case class Address(value: String Refined NonEmpty)
object Address {
  implicit val decoder: Decoder[Address] = Decoder.decodeString.emap(refineV[NonEmpty](_).map(Address(_)))
  implicit val encoder: Encoder[Address] = Encoder.encodeString.contramap(_.value.value)
}

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
case class Amount(value: String) extends AnyVal
object Amount {
  import io.circe.generic.extras.semiauto._
  implicit val decoder: Decoder[Amount] = deriveUnwrappedDecoder
  implicit val encoder: Encoder[Amount] = deriveUnwrappedEncoder
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


case class AddressInfo(balance: Balance, transactions: List[Transaction])
object AddressInfo {
  implicit val decoderTransfer: Decoder[AddressInfo] = deriveDecoder
  implicit val encoderTransfer: Encoder[AddressInfo] = deriveEncoder

  implicit def addressInfoEntityDecoder[F[_]](implicit F: Effect[F]): EntityDecoder[F, AddressInfo] =
    jsonOf[F, AddressInfo]
}

case class Balance(value: String) extends AnyVal
object Balance {
  import io.circe.generic.extras.semiauto._

  implicit val decoderTransfer: Decoder[Balance] = deriveUnwrappedDecoder
  implicit val encoderTransfer: Encoder[Balance] = deriveUnwrappedEncoder

  implicit def addressInfoEntityDecoder[F[_]](implicit F: Effect[F]): EntityDecoder[F, Balance] =
    jsonOf[F, Balance]
}
