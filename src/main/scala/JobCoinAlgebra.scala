package com.tbrown.jobcoin

import cats.ApplicativeError
import cats.effect.Effect

import eu.timepit.refined.collection.NonEmpty

import io.circe.syntax._

import org.http4s.Status.Successful
import org.http4s.{Headers, MediaType, Method, Request, Status, Uri}
import org.http4s.client.blaze.PooledHttp1Client
import org.http4s.headers._
import org.http4s.circe._

trait JobCoinAlgebra[F[_]] {
  def getAddressInfo(addr: Address): F[AddressInfo]
  def performTransaction(fromAddress: FromAddress, toAddress: ToAddress, amount: Amount): F[Unit]
  def getTransactions: F[List[Transaction]]
  def randomDepositAddress: F[DepositAddress]
}

class JobCoinInterpreter[F[_]](implicit F: Effect[F], AE: ApplicativeError[F, Throwable]) extends JobCoinAlgebra[F] {
  val addressUri = Uri.uri("http://jobcoin.gemini.com/hypocrisy/api/addresses/")
  val transactionsUri = Uri.uri("http://jobcoin.gemini.com/hypocrisy/api/transactions")

  val client = PooledHttp1Client[F]()

  def getAddressInfo(addr: Address): F[AddressInfo] = {
    client.expect[AddressInfo](addressUri / addr.value.value)
  }

  def performTransaction(fromAddress: FromAddress, toAddress: ToAddress, amount: Amount): F[Unit] = {
    val req = Request[F](method = Method.POST, uri = transactionsUri, headers = Headers(`Content-Type`(MediaType.`application/json`)))
      .withBody(Transfer(fromAddress, toAddress, amount).asJson)

    client.fetch(req) {
      case Successful(_) => F.delay(())
      case failedResponse => failedResponse.status match {
        case Status.UnprocessableEntity => AE.raiseError(InsufficientFunds)
        case _ => AE.raiseError(TransferFailed)
      }
    }
  }

  def getTransactions: F[List[Transaction]] =
    client.expect[List[Transaction]](transactionsUri)

  def randomDepositAddress: F[DepositAddress] = {
    import eu.timepit.refined._

    F.delay(DepositAddress(refineV[NonEmpty](java.util.UUID.randomUUID().toString).right.get))
  }
}
