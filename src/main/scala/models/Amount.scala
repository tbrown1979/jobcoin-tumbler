package com.tbrown.jobcoin

import io.circe._

case class Amount(value: String) extends AnyVal

object Amount {
  import io.circe.generic.extras.semiauto._
  implicit val decoder: Decoder[Amount] = deriveUnwrappedDecoder
  implicit val encoder: Encoder[Amount] = deriveUnwrappedEncoder
}
