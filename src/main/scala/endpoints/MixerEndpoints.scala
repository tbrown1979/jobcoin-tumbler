package com.tbrown.jobcoin

import cats.data.NonEmptyList
import cats.effect.Effect

import io.circe._
import io.circe.Decoder._
import io.circe.generic.semiauto._
import io.circe.refined._
import io.circe.syntax._

import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

import scala.language.higherKinds

class MixerEndpoints[F[_]: Effect](mixer: MixerService[F]) extends Http4sDsl[F] {
  import cats.implicits._

  implicit val entityEncoderDepositAddress: Encoder[DepositAddress] = deriveEncoder

  implicit def nonEmptyList[A: Decoder](implicit F: Effect[F]): EntityDecoder[F, NonEmptyList[A]] =
    jsonOf[F, NonEmptyList[A]](F, decodeNonEmptyList)

  val mixerEndpoints: HttpService[F] = HttpService[F] {
    case req @ POST -> Root / "addresses" =>
      for {
        userAddresses <- req.as[NonEmptyList[Address]]
        depAddr <- mixer.createMix(userAddresses)
        resp <- Ok(depAddr.asJson)
      } yield resp

    //not really the right place for ping, but oh well
    case GET -> Root / "ping" => Ok("pong")
  }
}