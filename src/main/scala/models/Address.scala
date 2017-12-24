package com.tbrown.jobcoin

//import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty


//JobCoin address
case class Address(value: String Refined NonEmpty)
