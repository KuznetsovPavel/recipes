package ru.pkuznetsov.ingredients.model

import io.circe.generic.semiauto.{deriveEncoder, deriveDecoder}
import io.circe.{Decoder, Encoder}

case class IngredientName(id: Int, name: String)

object IngredientName {
  implicit val decoder: Decoder[IngredientName] = deriveDecoder[IngredientName]
  implicit val encoder: Encoder[IngredientName] = deriveEncoder[IngredientName]
}
