package ru.pkuznetsov.ingredients.dao

import cats.data.NonEmptyList
import cats.effect.IO
import doobie.util.transactor.Transactor
import org.scalatest.tagobjects.Slow
import org.scalatest.{FunSuite, Matchers}
import ru.pkuznetsov.core.dao.DbTest

import scala.concurrent.ExecutionContext

class IngredientNamesPostgresQueriesTest extends FunSuite with Matchers with doobie.scalatest.IOChecker {

  implicit val cs = IO.contextShift(ExecutionContext.global)

  override def transactor = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:recipes",
    "pavel",
    "password"
  )

  test("insert ingredient name", Slow, DbTest) {
    check(IngredientNamesPostgresQueries.insert("banana"))
  }

  test("select ingredient name by id", Slow, DbTest) {
    check(IngredientNamesPostgresQueries.selectById(10))
  }

  test("select ingredient name by ids", Slow, DbTest) {
    check(IngredientNamesPostgresQueries.selectByIds(NonEmptyList.of(1, 2, 3, 4)))
  }

  test("select ingredient name by names", Slow, DbTest) {
    check(IngredientNamesPostgresQueries.selectByNames(NonEmptyList.of("one", "two", "three")))
  }

}
