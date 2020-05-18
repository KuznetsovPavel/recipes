package ru.pkuznetsov.recipes.api

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

final case class RecipeRequestBody(name: String,
                                   uri: Option[String],
                                   summary: String,
                                   author: String,
                                   cookingTime: Option[Int],
                                   calories: Option[Double],
                                   protein: Option[Double],
                                   fat: Option[Double],
                                   carbohydrates: Option[Double],
                                   sugar: Option[Double],
                                   ingredients: List[IngredientRequest])

object RecipeRequestBody {
  implicit val decoder: Decoder[RecipeRequestBody] = deriveDecoder[RecipeRequestBody]
  implicit val encoder: Encoder[RecipeRequestBody] = deriveEncoder[RecipeRequestBody]
}

final case class IngredientRequest(id: Int, amount: Double, unit: Option[String])

object IngredientRequest {
  implicit val decoder: Decoder[IngredientRequest] = deriveDecoder[IngredientRequest]
  implicit val encoder: Encoder[IngredientRequest] = deriveEncoder[IngredientRequest]
}
