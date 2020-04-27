package ru.pkuznetsov.recipes.dao

import cats.MonadError
import cats.effect.{Bracket, Resource}
import cats.implicits._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import ru.pkuznetsov.recipes.model.{Ingredient, Recipe}

class PostgresqlRecipeDao[F[_]](transactor: Resource[F, HikariTransactor[F]])
                               (implicit bracket: Bracket[F, Throwable],
                                monad: MonadError[F, Throwable]) {

  def createTables: F[Unit] = transactor.use { transactor =>
    for {
      _ <- PostgresqlRecipeQuires.createRecipesTable.transact(transactor)
      _ <- PostgresqlRecipeQuires.createIngredientNamesTable.transact(transactor)
      _ <- PostgresqlRecipeQuires.createIngredientsTable.transact(transactor)
    } yield ()
  }

  def insertRecipe(recipe: Recipe): F[Int] = transactor.use { transactor =>
    for {
      recipeId <- PostgresqlRecipeQuires.insertRecipe(recipe.uri.map(_.toString), recipe.summary, recipe.author, recipe.cookingTime, recipe.calories, recipe.protein, recipe.fat, recipe.carbohydrates,
        recipe.sugar).transact(transactor)
      _ <- recipe.ingredients.traverse(insertIngredient(_, recipeId))
    } yield recipeId
  }

  def insertIngredient(ingredient: Ingredient, recipeId: Int): F[Unit] = transactor.use { transactor =>
    for {
      optIngNameId <- PostgresqlRecipeQuires.selectIngredientNameId(ingredient.name).transact(transactor)
      ingNameId <- if (optIngNameId.isEmpty) PostgresqlRecipeQuires.insertIngredientName(ingredient.name).transact(transactor) else
        monad.pure(optIngNameId.get)
      _ <- PostgresqlRecipeQuires.insertIngredient(recipeId, ingNameId, ingredient.amount, ingredient.unit).transact(transactor)
    } yield ()
  }
}