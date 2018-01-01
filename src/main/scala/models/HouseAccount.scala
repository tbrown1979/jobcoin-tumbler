package com.tbrown.jobcoin

import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty

case class HouseAccount(value: String Refined NonEmpty)