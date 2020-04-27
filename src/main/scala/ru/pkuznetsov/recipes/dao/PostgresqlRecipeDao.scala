package ru.pkuznetsov.recipes.dao

import cats.MonadError
import cats.effect.{Bracket, Resource}
import cats.implicits._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import ru.pkuznetsov.recipes.model.Ingredient

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

  def insertIngredient(ingredient: Ingredient): F[Long] = transactor.use { transactor =>
    for {
      x <- PostgresqlRecipeQuires.insertIngredientIfNotExist(ingredient).transact(transactor)
      id <- PostgresqlRecipeQuires.getIngredientNameId(ingredient).unique.transact(transactor)
    } yield id
  }
}