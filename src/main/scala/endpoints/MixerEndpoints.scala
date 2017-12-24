package com.tbrown.jobcoin

import cats.data.NonEmptyList
import cats.effect.Effect

//import java.util.UUID

import io.circe._
//import io.circe.generic.auto._
//import io.circe.generic.extras.semiauto._
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.Decoder._

import io.circe.refined._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

import scala.language.higherKinds

class MixerEndpoints[F[_]: Effect](mixer: MixAlgebra[F]) extends Http4sDsl[F] {

  /* Need Instant Json Encoding */
//  import io.circe.java8.time._

  /* Needed for service composition via |+| */
  import cats.implicits._

  implicit val entityDecoderAddress: Decoder[Address] = deriveDecoder
  implicit val entityEncoderDepositAddress: Encoder[DepositAddress] = deriveEncoder



  //implicit val entityEncoderMix: Encoder[Mix] = deriveEncoder
//  implicit def entityEncoderDepositAddress[F[_]: Effect]: EntityEncoder[F, DepositAddress] = jsonEncoderOf[F, DepositAddress]

  implicit def nonEmptyList[A: Decoder](implicit F: Effect[F]): EntityDecoder[F, NonEmptyList[A]] =
    jsonOf[F, NonEmptyList[A]](F, decodeNonEmptyList)


  val mixerEndpoints: HttpService[F] = HttpService[F] {
    case req @ POST -> Root / "addresses" =>
      for {
        userAddresses <- req.as[NonEmptyList[Address]]
        depAddr <- mixer.addMix(userAddresses)
        resp <- Ok(depAddr.asJson)
      } yield resp

//    case GET -> Root / "hello" =>
//      for {
//        addresses <- mixer.getMixes
//        resp <- Ok(addresses)
//      } yield resp
  }

  /* We need to define an enum encoder and decoder since these do not come out of the box with generic derivation */
//  implicit val statusDecoder: Decoder[OrderStatus] = deriveEnumerationDecoder
//  implicit val statusEncoder: Encoder[OrderStatus] = deriveEnumerationEncoder

  /* Needed to decode entities */
//  implicit val orderDecoder = jsonOf[F, Order]

//  def placeOrderEndpoint(orderService: OrderService[F]): HttpService[F] =
//    HttpService[F] {
//      case req @ POST -> Root / "orders" => {
//        for {
//          order <- req.as[Order]
//          saved <- orderService.placeOrder(order)
//          resp <- Ok(saved.asJson)
//        } yield resp
//      }
//    }
//
//  def endpoints(orderService: OrderService[F]): HttpService[F] =
//    placeOrderEndpoint(orderService)
}

//object OrderEndpoints {
//  def endpoints[F[_]: Effect](orderService: OrderService[F]): HttpService[F] =
//    new OrderEndpoints[F].endpoints(orderService)
//}