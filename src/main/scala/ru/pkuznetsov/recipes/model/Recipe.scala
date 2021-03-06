package ru.pkuznetsov.recipes.model

import java.net.URI

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import ru.pkuznetsov.core.util.URIImplicits._

final case class Recipe(id: Int,
                        name: String,
                        uri: Option[URI],
                        summary: String,
                        author: String,
                        cookingTime: Option[Int],
                        calories: Option[Double],
                        protein: Option[Double],
                        fat: Option[Double],
                        carbohydrates: Option[Double],
                        sugar: Option[Double],
                        ingredients: List[Ingredient])

object Recipe {
  implicit val decoder: Decoder[Recipe] = deriveDecoder[Recipe]
  implicit val encoder: Encoder[Recipe] = deriveEncoder[Recipe]
}

final case class Ingredient(id: Int, name: String, amount: Double, unit: Option[String])

object Ingredient {
  implicit val decoder: Decoder[Ingredient] = deriveDecoder[Ingredient]
  implicit val encoder: Encoder[Ingredient] = deriveEncoder[Ingredient]
}
