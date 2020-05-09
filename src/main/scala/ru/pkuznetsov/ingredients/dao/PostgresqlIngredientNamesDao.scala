package ru.pkuznetsov.ingredients.dao

import cats.data.NonEmptyList
import cats.effect.{Bracket, Resource}
import doobie.hikari.HikariTransactor
import ru.pkuznetsov.core.dao.Dao
import ru.pkuznetsov.ingredients.model.IngredientName
import ru.pkuznetsov.recipes.services.RecipeService.IngredientId

import scala.language.implicitConversions

trait IngredientNamesDao[F[_]] {
  def getByNames(names: NonEmptyList[String]): F[List[IngredientName]]
  def getByIds(names: NonEmptyList[Int]): F[List[IngredientName]]
}

class PostgresqlIngredientNamesDao[F[_]](transactor: Resource[F, HikariTransactor[F]])(
    implicit bracket: Bracket[F, Throwable])
    extends Dao[F](transactor)
    with IngredientNamesDao[F] {

  def getByNames(names: NonEmptyList[String]): F[List[IngredientName]] =
    IngredientNamesPostgresQueries
      .selectByNames(names)
      .to[List]
      .map(listOfPair2ListOfIngNames)

  def getByIds(names: NonEmptyList[Int]): F[List[IngredientName]] =
    IngredientNamesPostgresQueries
      .selectByIds(names)
      .to[List]
      .map(listOfPair2ListOfIngNames)

  private def listOfPair2ListOfIngNames(list: List[(Int, String)]): List[IngredientName] =
    list.map {
      case (i, str) => IngredientName(IngredientId(i), str)
    }

  def insertIngName(name: String): F[Int] =
    IngredientNamesPostgresQueries
      .insert(name)
      .withUniqueGeneratedKeys[Int]("id")
}
