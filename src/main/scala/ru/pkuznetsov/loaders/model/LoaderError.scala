package ru.pkuznetsov.loaders.model

import io.circe.{Json, Printer}
import ru.pkuznetsov.core.model.AppError
import ru.pkuznetsov.loaders.connectors.spoonacular.SpoonacularLoader.LoaderRecipeId

sealed trait LoaderError extends AppError

object LoaderError {
  final case class CannotParseJson(json: Json) extends LoaderError
  final case class CannotFindIngredient(name: String) extends LoaderError
  final case class SpoonacularLoaderError(recipeId: LoaderRecipeId) extends LoaderError

  def handleError =
    (e: LoaderError) =>
      e match {
        case CannotParseJson(json)      => s"can not parse ${json.printWith(Printer.spaces2)}"
        case CannotFindIngredient(name) => s"can not find ingredient with name: $name"
        case SpoonacularLoaderError(id) => s"can not load recipe with id: $id"
    }
}
