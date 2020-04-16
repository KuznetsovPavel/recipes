package ru.pkuznetsov.recipes.dao

import cats.MonadError
import cats.effect.{Bracket, Resource}
import cats.implicits._
import doobie.hikari.HikariTransactor
import doobie.implicits._

class PostgresqlRecipeDao[F[_]](transactor: Resource[F, HikariTransactor[F]])
                               (implicit bracket: Bracket[F, Throwable],
                                monad: MonadError[F, Throwable]) {

  def createTables = transactor.use { transactor =>
    for {
      _ <- PostgresqlRecipeQuires.createRecipesTable.transact(transactor)
      _ <- PostgresqlRecipeQuires.createIngredientNamesTable.transact(transactor)
      _ <- PostgresqlRecipeQuires.createIngredientsTable.transact(transactor)
    } yield ()

  }

}
