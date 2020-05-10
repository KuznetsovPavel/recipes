package ru.pkuznetsov.core.model

import ru.pkuznetsov.bucket.model.BucketError
import ru.pkuznetsov.ingredients.model.IngredientError
import ru.pkuznetsov.loaders.model.LoaderError
import ru.pkuznetsov.recipes.model.RecipeError

trait AppError extends Throwable

object AppError {
  def handleError: PartialFunction[Throwable, String] = {
    case e: RecipeError     => RecipeError.handleError(e)
    case e: IngredientError => IngredientError.handleError(e)
    case e: BucketError     => BucketError.handleError(e)
    case e: LoaderError     => LoaderError.handleError(e)
  }
}
