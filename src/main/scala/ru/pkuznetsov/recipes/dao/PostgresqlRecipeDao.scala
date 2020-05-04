package ru.pkuznetsov.recipes.dao

import cats.MonadError
import cats.effect.{Bracket, Resource}
import cats.implicits._
import doobie.hikari.HikariTransactor
import doobie.implicits._
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

  implicit def recipe2RecipeRow(recipe: Recipe): RecipeRow =
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
    transactor.use { transactor =>
      for {
        recipeRow <- PostgresqlRecipeQuires.selectRecipe(recipeId).option.transact(transactor)
        ingRows <- PostgresqlRecipeQuires.selectIngredient(recipeId).to[List].transact(transactor)
        names <- ingRows
          .map(_.ingredientId)
          .traverse(id => PostgresqlRecipeQuires.selectIngredientName(id).option)
          .transact(transactor)
      } yield createRecipeFrom(recipeRow, ingRows, names.flatten.toMap)

    }
  }

  def createRecipeFrom(recipeRow: Option[RecipeRow],
                       ingredients: List[IngredientRow],
                       names: Map[Int, String]): Recipe = ??? //{
//    val ings = ingredients.map {row =>
//      Ingredient(row.ingredientId, row.)
//    }
//  }

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
