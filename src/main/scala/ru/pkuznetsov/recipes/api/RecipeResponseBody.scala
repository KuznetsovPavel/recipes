package ru.pkuznetsov.recipes.api

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import ru.pkuznetsov.recipes.model.Recipe

final case class RecipeResponseBody(name: String,
                                    uri: Option[String],
                                    summary: String,
                                    author: String,
                                    cookingTime: Option[Int],
                                    calories: Option[Double],
                                    protein: Option[Double],
                                    fat: Option[Double],
                                    carbohydrates: Option[Double],
                                    sugar: Option[Double],
                                    ingredients: List[IngredientResponse])

object RecipeResponseBody {
  def fromRecipe(recipe: Recipe): RecipeResponseBody =
    RecipeResponseBody(
      recipe.name,
      recipe.uri.map(_.toString),
      recipe.summary,
      recipe.author,
      recipe.cookingTime,
      recipe.calories,
      recipe.protein,
      recipe.fat,
      recipe.carbohydrates,
      recipe.sugar,
      recipe.ingredients.map(ing =>
        IngredientResponse(ing.name, if (ing.amount == 0.0) None else Some(ing.amount), ing.unit))
    )

  implicit val decoder: Decoder[RecipeResponseBody] = deriveDecoder[RecipeResponseBody]
  implicit val encoder: Encoder[RecipeResponseBody] = deriveEncoder[RecipeResponseBody]
}

final case class IngredientResponse(name: String, amount: Option[Double], unit: Option[String])

object IngredientResponse {
  implicit val decoder: Decoder[IngredientResponse] = deriveDecoder[IngredientResponse]
  implicit val encoder: Encoder[IngredientResponse] = deriveEncoder[IngredientResponse]
}
