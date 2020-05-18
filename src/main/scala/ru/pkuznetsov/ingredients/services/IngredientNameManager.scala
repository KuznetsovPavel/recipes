package ru.pkuznetsov.ingredients.services

import cats.MonadError
import cats.data.NonEmptyList
import cats.effect.Sync
import cats.instances.list._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import com.typesafe.scalalogging.StrictLogging
import ru.pkuznetsov.ingredients.dao.IngredientNamesDao
import ru.pkuznetsov.ingredients.model.IngredientError._
import ru.pkuznetsov.ingredients.model.{IngredientError, IngredientName}
import ru.pkuznetsov.recipes.services.RecipeService.IngredientId

trait IngredientNameManager[F[_]] {
  def getIngredientIdsFor(names: List[String]): F[List[IngredientName]]
  def getIngredientNamesFor(ids: List[IngredientId]): F[List[IngredientName]]
  def addAndGetNames(names: List[String]): F[List[IngredientName]]
  def checkIngredientNames(names: List[String]): F[Unit]
  def checkIngredientIds(ids: List[IngredientId]): F[Unit]
}

class IngredientNameManagerImpl[F[_]: Sync](dao: IngredientNamesDao[F])(
    implicit monad: MonadError[F, Throwable])
    extends IngredientNameManager[F]
    with StrictLogging {

  def getIngredientIdsFor(names: List[String]): F[List[IngredientName]] =
    for {
      nonEmptyIngredients <- monad.fromOption(NonEmptyList.fromList(names), EmptyIngredientList)
      _ <- errorIfEmpty(getDuplicates(names), IngredientNameDuplicates)
      ingNames <- dao.getByNames(nonEmptyIngredients)
      _ <- errorIfEmpty(names.diff(ingNames.map(_.name)), CannotFindIngredientNames)
    } yield ingNames

  def getIngredientNamesFor(ids: List[IngredientId]): F[List[IngredientName]] =
    for {
      nonEmptyIngredients <- monad.fromOption(NonEmptyList.fromList(ids), EmptyIngredientList)
      _ <- errorIfEmpty(getDuplicates(ids), IngredientIdsDuplicates)
      ingNames <- dao.getByIds(nonEmptyIngredients)
      _ <- errorIfEmpty(ids.diff(ingNames.map(_.id)), CannotFindIngredientIds)
    } yield ingNames

  def addAndGetNames(names: List[String]): F[List[IngredientName]] =
    for {
      nonEmptyIngredients <- monad.fromOption(NonEmptyList.fromList(names), EmptyIngredientList)
      oldIngNames <- dao.getByNames(nonEmptyIngredients)
      newNames <- monad.pure(names.diff(oldIngNames.map(_.name)))
      _ <- newNames match {
        case Nil  => Sync[F].delay(logger.debug("find add ingredients"))
        case list => Sync[F].delay(logger.debug(s"can not find ingredients ${list.mkString(", ")}"))
      }
      newIngNames <- insertNames(newNames)
    } yield oldIngNames ++ newIngNames

  def checkIngredientNames(names: List[String]): F[Unit] =
    getIngredientIdsFor(names).map(_ => ())

  def checkIngredientIds(ids: List[IngredientId]): F[Unit] =
    getIngredientNamesFor(ids).map(_ => ())

  private def insertNames(names: List[String]): F[List[IngredientName]] =
    names.traverse { name =>
      dao.insertIngName(name).flatMap(id => monad.pure(IngredientName(IngredientId(id), name)))
    }

  private def errorIfEmpty[A, E <: IngredientError](list: List[A], error: List[A] => E): F[Unit] =
    list match {
      case Nil  => monad.unit
      case list => monad.raiseError[Unit](error(list))
    }

  private def getDuplicates[A: Ordering](list: List[A]): List[A] =
    list.groupBy(identity).filter(_._2.size != 1).keys.toList

}
