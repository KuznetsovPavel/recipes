package ru.pkuznetsov.recipes.model

sealed trait Errors extends Throwable

object Errors {
  final case class CannotParseData(cause: Throwable) extends Errors
  final case class SpoonacularError(cause: Throwable) extends Errors
  final case class RecipeNotExist(id: Int) extends Errors
  final case class CannotFindIngredientName(ingredientId: Int) extends Errors
  final case class CannotParseURI(uri: String, recipeId: Int) extends Errors
}
