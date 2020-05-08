package ru.pkuznetsov.recipes.model

import ru.pkuznetsov.core.model.AppError
import ru.pkuznetsov.recipes.services.RecipeService.{IngredientId, RecipeId}

sealed trait RecipeError extends AppError

object RecipeError {
  final case class CannotFindIngredient(id: IngredientId) extends RecipeError
  final case class CannotParseURI(uri: String, recipeId: RecipeId) extends RecipeError
  final case class RecipeNotExist(id: RecipeId) extends RecipeError
}
