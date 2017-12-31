package com.tbrown.jobcoin

import cats.data.NonEmptyList
//import com.banno.symxchange.common.ErrorArbitraries.symXchangeEmptyFieldGen
import org.scalacheck.{Arbitrary, Gen}

trait ModelArbitraries {

  import Gen._

  val sizeIdentifier = 127

  def eitherGen[E, A](leftGen: Gen[E], rightGen: Gen[A]): Gen[Either[E, A]] = {
    val leftG: Gen[Either[E, A]]  = leftGen.map(Left(_))
    val rightG: Gen[Either[E, A]] = rightGen.map(Right(_))
    oneOf(leftG, rightG)
  }

  def nonEmptyListGen[T](implicit G: Gen[T]): Gen[NonEmptyList[T]] =
    for {
      head <- G
      tail <- listOf[T](G)
    } yield NonEmptyList(head, tail)
}