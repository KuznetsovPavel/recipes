package ru.pkuznetsov.core.model

import ru.pkuznetsov.recipes.model.Recipe

trait AppError extends Throwable

object AppError {
  final case class CannotParseData(cause: Throwable) extends AppError
  final case class SpoonacularError(cause: Throwable) extends AppError
  final case class IncorrectRecipeId(id: String) extends AppError
  final case class RecipeShouldHaveIngredients(recipe: Recipe) extends AppError
  final object EmptyBucket extends AppError
}
