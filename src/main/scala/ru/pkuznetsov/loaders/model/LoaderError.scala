package ru.pkuznetsov.loaders.model

import io.circe.Json
import ru.pkuznetsov.core.model.AppError
import ru.pkuznetsov.loaders.connectors.spoonacular.SpoonacularLoader.LoaderRecipeId

sealed trait LoaderError extends AppError

object LoaderError {
  final case class CannotParseJson(json: Json) extends LoaderError
  final case class CannotFindIngredient(name: String) extends LoaderError
  final case class DuplicateIngredients(recipeId: LoaderRecipeId, ingName: String) extends LoaderError
  final case class SpoonacularLoaderError(recipeId: LoaderRecipeId) extends LoaderError
  final case class SpoonacularRecipeNotFound(recipeId: LoaderRecipeId) extends LoaderError
  object ApiKeyExpired extends LoaderError
}
