package ru.pkuznetsov.ingredients.services

import cats.MonadError
import cats.data.NonEmptyList
import cats.syntax.flatMap._
import cats.syntax.functor._
import ru.pkuznetsov.ingredients.dao.PostgresqlIngredientNamesDao
import ru.pkuznetsov.ingredients.model.IngredientError._
import ru.pkuznetsov.ingredients.model.{IngredientError, IngredientName}
import ru.pkuznetsov.recipes.services.RecipeService.IngredientId

class IngredientNameManager[F[_]](dao: PostgresqlIngredientNamesDao[F])(
    implicit monad: MonadError[F, Throwable]) {

  def getIngredientIdsFor(names: List[String]): F[List[IngredientName]] =
    for {
      nonEmptyIngredients <- monad.fromOption(NonEmptyList.fromList(names), EmptyIngredientList)
      _ <- errorIfEmpty(getDuplicates(names), IngredientNameDuplicates)
      ingNames <- dao.getByNames(nonEmptyIngredients)
      _ <- errorIfEmpty(getDiff(names, ingNames.map(_.name)), CannotFindIngredientNames)
    } yield ingNames

  def getIngredientNamesFor(ids: List[Int]): F[List[IngredientName]] =
    for {
      nonEmptyIngredients <- monad.fromOption(NonEmptyList.fromList(ids), EmptyIngredientList)
      _ <- errorIfEmpty(getDuplicates(ids), IngredientIdsDuplicates)
      ingNames <- dao.getByIds(nonEmptyIngredients)
      _ <- errorIfEmpty(getDiff(ids, ingNames.map(_.id)), CannotFindIngredientIds)
    } yield ingNames

  def checkIngredientNames(names: List[String]): F[Unit] =
    getIngredientIdsFor(names).map(_ => ())

  def checkIngredientIds(ids: List[IngredientId]): F[Unit] =
    getIngredientNamesFor(ids).map(_ => ())

  private def errorIfEmpty[A, E <: IngredientError](list: List[A], error: List[A] => E): F[Unit] =
    list match {
      case Nil  => monad.unit
      case list => monad.raiseError[Unit](error(list))
    }

  private def getDiff[A](init: List[A], target: List[A]): List[A] = init.diff(target)

  private def getDuplicates[A: Ordering](list: List[A]): List[A] =
    list.groupBy(identity).filter(_._2.size != 1).keys.toList

}
