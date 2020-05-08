package ru.pkuznetsov.ingredients.model

import ru.pkuznetsov.core.model.AppError

sealed trait IngredientError extends AppError

object IngredientError {
  final case class IngredientNameDuplicates(names: List[String]) extends IngredientError
  final case class IngredientIdsDuplicates(ids: List[Int]) extends IngredientError
  object EmptyIngredientList extends IngredientError
  final case class CannotFindIngredientNames(names: List[String]) extends IngredientError
  final case class CannotFindIngredientIds(ids: List[Int]) extends IngredientError

  def handleError =
    (e: IngredientError) =>
      e match {
        case IngredientNameDuplicates(names)  => s"duplicates in ingredient names ${names.mkString(", ")}"
        case IngredientIdsDuplicates(ids)     => s"duplicates in ingredient with ids ${ids.mkString(", ")}"
        case EmptyIngredientList              => "ingredient list is empty"
        case CannotFindIngredientNames(names) => s"can not find ingredient with names ${names.mkString(", ")}"
        case CannotFindIngredientIds(ids)     => s"can not find ingredient with ids ${ids.mkString(", ")}"
    }
}
