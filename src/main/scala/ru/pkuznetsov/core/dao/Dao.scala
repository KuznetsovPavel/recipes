package ru.pkuznetsov.core.dao

import cats.effect.{Bracket, Resource}
import cats.free.Free
import doobie.free.connection
import doobie.free.connection.ConnectionOp
import doobie.hikari.HikariTransactor
import doobie.implicits.toConnectionIOOps
import ru.pkuznetsov.core.model.Ingredient

import scala.language.implicitConversions

abstract class Dao[F[_]](transactor: Resource[F, HikariTransactor[F]])(
    implicit bracket: Bracket[F, Throwable]) {

  implicit def connectionIO2F[A](transaction: Free[connection.ConnectionOp, A]): F[A] =
    transactor.use(transactor => transaction.transact(transactor))

  def checkIngNameAnd[A](ing: Ingredient)(f: Int => Free[ConnectionOp, A]) =
    for {
      optIngNameId <- getIngNameId(ing.name)
      result <- optIngNameId match {
        case Some(value) => f(value)
        case None        => insertIngName(ing.name).flatMap(x => f(x))
      }
    } yield result

  def getIngNameId(name: String) =
    CommonPostgresQueries
      .selectIngredientNameId(name)
      .option

  def insertIngName(name: String): doobie.ConnectionIO[Int] =
    CommonPostgresQueries
      .insertIngredientName(name)
      .withUniqueGeneratedKeys[Int]("id")
}
