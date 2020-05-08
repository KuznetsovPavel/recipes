package ru.pkuznetsov.ingredients.model

import ru.pkuznetsov.core.model.AppError

sealed trait IngredientError extends AppError

object IngredientError {
  final case class IngredientNameDuplicates(names: List[String]) extends IngredientError
  final case class IngredientIdsDuplicates(ids: List[Int]) extends IngredientError
  object EmptyIngredientList extends IngredientError
  final case class CannotFindIngredientNames(names: List[String]) extends IngredientError
  final case class CannotFindIngredientIds(names: List[Int]) extends IngredientError
}
