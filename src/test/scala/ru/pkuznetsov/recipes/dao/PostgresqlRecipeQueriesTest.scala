package ru.pkuznetsov.recipes.dao

import cats.effect.IO
import doobie.util.transactor.Transactor
import org.scalatest.tagobjects.Slow
import org.scalatest.{FunSuite, Matchers, Tag}

import scala.concurrent.ExecutionContext

object DbTest extends Tag("DbTest")

class PostgresqlRecipeQueriesTest extends FunSuite with Matchers with doobie.scalatest.IOChecker {
  implicit val cs = IO.contextShift(ExecutionContext.global)

  override def transactor = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:recipes",
    "pavel",
    "password"
  )

  test("create ingredients table", Slow, DbTest) {
    check(PostgresqlRecipeQueries.createIngredientsTable)
  }

  test("create ingredient names table", Slow, DbTest) {
    check(PostgresqlRecipeQueries.createIngredientNamesTable)
  }

  test("create recipes table", Slow, DbTest) {
    check(PostgresqlRecipeQueries.createRecipesTable)
  }

  test("insert recipe", Slow, DbTest) {
    check(
      PostgresqlRecipeQueries.insertRecipe(
        "pizza",
        None,
        "it is perfect",
        "Pavel",
        Some(40),
        Some(100.0),
        None,
        None,
        Some(33.3),
        Some(12.34)
      )
    )
  }

  test("insert ingredient", Slow, DbTest) {
    check(PostgresqlRecipeQueries.insertIngredient(10, 100, 123.23, Some("ml")))
  }

  test("insert ingredient name", Slow, DbTest) {
    check(PostgresqlRecipeQueries.insertIngredientName("banana"))
  }

  test("select ingredient", Slow, DbTest) {
    check(PostgresqlRecipeQueries.selectIngredientNameId("banana"))
  }

}
