package ru.pkuznetsov.core.dao

import cats.effect.IO
import doobie.util.transactor.Transactor
import org.scalatest.tagobjects.Slow
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.ExecutionContext

class CommonPostgresQueriesTest extends FunSuite with Matchers with doobie.scalatest.IOChecker {

  implicit val cs = IO.contextShift(ExecutionContext.global)

  override def transactor = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:recipes",
    "pavel",
    "password"
  )

  test("insert ingredient name", Slow, DbTest) {
    check(CommonPostgresQueries.insertIngredientName("banana"))
  }

  test("select ingredient name", Slow, DbTest) {
    check(CommonPostgresQueries.selectIngredientNameId("banana"))
  }

  test("select ingredient name id ", Slow, DbTest) {
    check(CommonPostgresQueries.selectIngredientNameId("banana"))
  }

}
