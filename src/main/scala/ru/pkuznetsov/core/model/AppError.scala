package ru.pkuznetsov.core.model

import ru.pkuznetsov.ingredients.model.IngredientError
import ru.pkuznetsov.recipes.model.RecipeError

trait AppError extends Throwable

object AppError {
  final case class CannotParseData(cause: Throwable) extends AppError
  final case class SpoonacularError(cause: Throwable) extends AppError
  final object EmptyBucket extends AppError

  def handleError: PartialFunction[Throwable, String] = {
    case e: RecipeError     => RecipeError.handleError(e)
    case e: IngredientError => IngredientError.handleError(e)
  }
}
