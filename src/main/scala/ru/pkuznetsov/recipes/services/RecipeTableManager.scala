package ru.pkuznetsov.recipes.services

import java.net.URI

import cats.MonadError
import cats.instances.list._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import ru.pkuznetsov.ingredients.model.IngredientName
import ru.pkuznetsov.recipes.api.{IngredientRequest, RecipeRequestBody}
import ru.pkuznetsov.recipes.dao.{IngredientRow, RecipeRow}
import ru.pkuznetsov.recipes.model.RecipeError.{CannotFindIngredient, CannotParseURI}
import ru.pkuznetsov.recipes.model.{Ingredient, Recipe}
import ru.pkuznetsov.recipes.services.RecipeService.{IngredientId, RecipeId}

import scala.util.Try

trait RecipeTableManager[F[_]] {
  def createRecipeFrom(recipeRow: RecipeRow,
                       ingredients: List[IngredientRow],
                       names: List[IngredientName]): F[Recipe]
  def recipe2RecipeRow(recipe: Recipe): RecipeRow
  def recipeRequest2RecipeRow(recipe: RecipeRequestBody): RecipeRow
  def ingRequest2IngRow(ingRequest: IngredientRequest, recipeId: RecipeId): IngredientRow
}

class RecipeTableManagerImpl[F[_]](implicit monad: MonadError[F, Throwable]) extends RecipeTableManager[F] {

  def createRecipeFrom(recipeRow: RecipeRow,
                       ingredients: List[IngredientRow],
                       names: List[IngredientName]): F[Recipe] = {
    def createIngredients: F[List[Ingredient]] =
      ingredients.traverse { row =>
        names.find(_.id == row.ingredientId) match {
          case Some(ingName) => monad.pure(Ingredient(row.ingredientId, ingName.name, row.amount, row.`unit`))
          case None          => monad.raiseError[Ingredient](CannotFindIngredient(IngredientId(row.ingredientId)))
        }
      }

    def createURI(recipeRow: RecipeRow): F[Option[URI]] =
      Try(recipeRow.uri.map(URI.create)).toOption match {
        case Some(uri) => monad.pure(uri)
        case None      => monad.raiseError(CannotParseURI(recipeRow.uri.get, RecipeId(recipeRow.recipeId)))
      }

    for {
      ings <- createIngredients
      uri <- createURI(recipeRow)
    } yield recipeRow2Recipe(recipeRow, uri, ings)

  }

  private def recipeRow2Recipe(recipeRow: RecipeRow, uri: Option[URI], ings: List[Ingredient]) =
    Recipe(
      recipeRow.recipeId,
      recipeRow.name,
      uri,
      recipeRow.summary,
      recipeRow.author,
      recipeRow.cookingTime,
      recipeRow.calories,
      recipeRow.protein,
      recipeRow.fat,
      recipeRow.carbohydrates,
      recipeRow.sugar,
      ings
    )

  def recipe2RecipeRow(recipe: Recipe): RecipeRow =
    RecipeRow(
      recipe.id,
      recipe.name,
      recipe.uri.map(_.toString),
      recipe.summary,
      recipe.author,
      recipe.cookingTime,
      recipe.calories,
      recipe.protein,
      recipe.fat,
      recipe.carbohydrates,
      recipe.sugar
    )

  def recipeRequest2RecipeRow(recipe: RecipeRequestBody): RecipeRow =
    RecipeRow(
      0,
      recipe.name,
      recipe.uri.map(_.toString),
      recipe.summary,
      recipe.author,
      recipe.cookingTime,
      recipe.calories,
      recipe.protein,
      recipe.fat,
      recipe.carbohydrates,
      recipe.sugar
    )

  def ingRequest2IngRow(ingRequest: IngredientRequest, recipeId: RecipeId): IngredientRow =
    IngredientRow(recipeId, ingRequest.id, ingRequest.amount, ingRequest.unit)

}
