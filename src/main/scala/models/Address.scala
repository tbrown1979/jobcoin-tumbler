package com.tbrown.jobcoin

import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty

import io.circe._

//no semi-auto deriviation of enc/dec with shapeless-tagged refinements. Resorting to some boilerplate
//https://github.com/circe/circe/issues/220
//also can't derive enc/dec for value classes containing refined types
case class Address(value: String Refined NonEmpty)
object Address {
  implicit val decoder: Decoder[Address] = Decoder.decodeString.emap(refineV[NonEmpty](_).map(Address(_)))
  implicit val encoder: Encoder[Address] = Encoder.encodeString.contramap(_.value.value)
}