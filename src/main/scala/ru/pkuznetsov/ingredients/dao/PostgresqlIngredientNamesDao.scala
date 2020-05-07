package ru.pkuznetsov.ingredients.dao

import cats.data.NonEmptyList
import cats.effect.{Bracket, Resource}
import doobie.hikari.HikariTransactor
import ru.pkuznetsov.core.dao.Dao

import scala.language.implicitConversions

class PostgresqlIngredientNamesDao[F[_]](transactor: Resource[F, HikariTransactor[F]])(
    implicit bracket: Bracket[F, Throwable])
    extends Dao[F](transactor) {

  def getByNames(names: NonEmptyList[String]): F[List[(Int, String)]] =
    IngredientNamesPostgresQueries
      .selectByNames(names)
      .to[List]

  def getByIds(names: NonEmptyList[Int]): F[List[(Int, String)]] =
    IngredientNamesPostgresQueries
      .selectByIds(names)
      .to[List]

  def insertIngName(name: String): F[Int] =
    IngredientNamesPostgresQueries
      .insert(name)
      .withUniqueGeneratedKeys[Int]("id")
}
