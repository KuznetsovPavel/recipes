package ru.pkuznetsov.core.model

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

final case class Ingredient(id: Long, name: String, amount: Double, unit: Option[String])

object Ingredient {
  implicit val decoder: Decoder[Ingredient] = deriveDecoder[Ingredient]
  implicit val encoder: Encoder[Ingredient] = deriveEncoder[Ingredient]
}
