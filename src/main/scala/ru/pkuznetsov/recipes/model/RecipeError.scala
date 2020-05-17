package ru.pkuznetsov.recipes.model

import ru.pkuznetsov.core.model.AppError
import ru.pkuznetsov.recipes.services.RecipeService.{IngredientId, RecipeId}

sealed trait RecipeError extends AppError

object RecipeError {
  final case class CannotFindIngredient(id: IngredientId) extends RecipeError
  final case class CannotParseURI(uri: String, recipeId: RecipeId) extends RecipeError
  final case class RecipeNotExist(id: RecipeId) extends RecipeError
  object EmptyBucket extends RecipeError
  object BucketNotExist extends RecipeError
  object IncorrectMissingIngredients extends RecipeError

  def handleError =
    (e: RecipeError) =>
      e match {
        case RecipeError.CannotFindIngredient(id)    => s"can not find ingredient with id $id"
        case RecipeError.CannotParseURI(_, _)        => "incorrect uri"
        case RecipeError.RecipeNotExist(id)          => s"recipe with id $id not exist"
        case RecipeError.BucketNotExist              => "bucket not exist"
        case RecipeError.EmptyBucket                 => "empty bucket"
        case RecipeError.IncorrectMissingIngredients => "missing ingredient count should be positive"
    }
}
