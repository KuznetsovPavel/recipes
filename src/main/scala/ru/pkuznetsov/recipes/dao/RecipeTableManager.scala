package ru.pkuznetsov.recipes.dao

import java.net.URI

import cats.MonadError
import cats.instances.list._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.monadError._
import cats.syntax.traverse._
import ru.pkuznetsov.core.model.Ingredient
import ru.pkuznetsov.recipes.model.Errors.{CannotFindIngredientName, CannotParseURI, RecipeNotExist}
import ru.pkuznetsov.recipes.model.Recipe
import ru.pkuznetsov.recipes.services.RecipeService.RecipeId

class RecipeTableManager[F[_]](implicit monad: MonadError[F, Throwable]) {

  def createRecipeFrom(recipeId: RecipeId,
                       recipeRow: Option[RecipeRow],
                       ingredients: List[IngredientRow],
                       names: Map[Int, String]): F[Recipe] = {
    def createIngredients: F[List[Ingredient]] = {
      ingredients.traverse { row =>
        val ing = names.get(row.ingredientId).map { name =>
          Ingredient(row.ingredientId, name, row.amount, row.`unit`)
        }
        monad.fromOption(ing, CannotFindIngredientName(row.ingredientId))
      }
    }

    def createURI(recipeRow: RecipeRow): F[Option[URI]] = {
      monad.catchNonFatal(recipeRow.uri.map(URI.create)).adaptError {
        case _ => CannotParseURI(recipeRow.uri.get, recipeRow.recipeId)
      }
    }

    for {
      recipe <- monad.fromOption(recipeRow, RecipeNotExist(recipeId))
      ings <- createIngredients
      uri <- createURI(recipe)
    } yield recipeRow2Recipe(recipe, uri, ings)

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

}
