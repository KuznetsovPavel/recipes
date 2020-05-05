package ru.pkuznetsov.recipes.dao

import java.net.URI

import cats.MonadError
import cats.effect.{Bracket, Resource}
import cats.implicits._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import ru.pkuznetsov.recipes.model.Errors.{CannotFindIngredientName, CannotParseURI, RecipeNotExist}
import ru.pkuznetsov.recipes.model.{Ingredient, Recipe}

import scala.language.implicitConversions

class PostgresqlRecipeDao[F[_]](transactor: Resource[F, HikariTransactor[F]])(
    implicit bracket: Bracket[F, Throwable],
    monad: MonadError[F, Throwable]
) {

  def createTables: F[Unit] = transactor.use { transactor =>
    for {
      _ <- PostgresqlRecipeQueries.createRecipesTable.run.transact(transactor)
      _ <- PostgresqlRecipeQueries.createIngredientNamesTable.run.transact(transactor)
      _ <- PostgresqlRecipeQueries.createIngredientsTable.run.transact(transactor)
    } yield ()
  }

  def insertRecipe(recipe: Recipe): F[Int] = transactor.use { transactor =>
    for {
      recipeId <- PostgresqlRecipeQueries
        .insertRecipe(recipe)
        .withUniqueGeneratedKeys[Int]("id")
        .transact(transactor)
      _ <- recipe.ingredients.traverse(insertIngredient(_, recipeId))
    } yield recipeId
  }

  private implicit def recipe2RecipeRow(recipe: Recipe): RecipeRow =
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

  def selectRecipe(recipeId: Int): F[Recipe] = {

    def recipeRow2Recipe(recipeRow: RecipeRow, uri: Option[URI], ings: List[Ingredient]) =
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

    def createRecipeFrom(recipeRow: RecipeRow,
                         ingredients: List[IngredientRow],
                         names: Map[Int, String]): F[Recipe] = {
      val ings = ingredients.traverse { row =>
        val ing = names.get(row.ingredientId).map { name =>
          Ingredient(row.ingredientId, name, row.amount, row.`unit`)
        }
        monad.fromOption(ing, CannotFindIngredientName(row.ingredientId))
      }

      val uri = monad.catchNonFatal(recipeRow.uri.map(URI.create)).adaptError {
        case _ => CannotParseURI(recipeRow.uri.get, recipeRow.recipeId)
      }

      monad.map2(uri, ings) {
        case (uri, ingredients) => recipeRow2Recipe(recipeRow, uri, ingredients)
      }
    }

    transactor.use { transactor =>
      for {
        recipeRowOpt <- PostgresqlRecipeQueries.selectRecipe(recipeId).option.transact(transactor)
        recipeRow <- monad.fromOption(recipeRowOpt, RecipeNotExist(recipeId))
        ingRows <- PostgresqlRecipeQueries.selectIngredient(recipeId).to[List].transact(transactor)
        names <- ingRows
          .map(_.ingredientId)
          .traverse(id => PostgresqlRecipeQueries.selectIngredientName(id).option)
          .transact(transactor)
        recipe <- createRecipeFrom(recipeRow, ingRows, names.flatten.toMap)
      } yield recipe
    }
  }

  def insertIngredient(ingredient: Ingredient, recipeId: Int): F[Unit] = {
    def getIngNameId =
      PostgresqlRecipeQueries
        .selectIngredientNameId(ingredient.name)
        .option

    def insertIngName =
      PostgresqlRecipeQueries
        .insertIngredientName(ingredient.name)
        .withUniqueGeneratedKeys[Int]("id")

    def insertIng(ingNameId: Int) =
      PostgresqlRecipeQueries
        .insertIngredient(IngredientRow(recipeId, ingNameId, ingredient.amount, ingredient.unit))
        .run

    transactor.use { transactor =>
      for {
        optIngNameId <- getIngNameId.transact(transactor)
        ingNameId <- if (optIngNameId.isEmpty) insertIngName.transact(transactor)
        else monad.pure(optIngNameId.get)
        _ <- insertIng(ingNameId).transact(transactor)
      } yield ()
    }
  }
}
