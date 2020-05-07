package ru.pkuznetsov.core.model

import ru.pkuznetsov.recipes.model.Recipe

sealed trait Errors extends Throwable

object Errors {
  final case class CannotParseData(cause: Throwable) extends Errors
  final case class SpoonacularError(cause: Throwable) extends Errors
  final case class RecipeNotExist(id: Int) extends Errors
  final case class CannotFindIngredients(nameOrId: List[String]) extends Errors
  final case class CannotParseURI(uri: String, recipeId: Int) extends Errors
  final case class IncorrectRecipeId(id: String) extends Errors
  final case class IngredientDuplicates(namesOrIds: List[String]) extends Errors
  final case class RecipeShouldHaveIngredients(recipe: Recipe) extends Errors
  final object EmptyBucket extends Errors
}
