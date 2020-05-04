package ru.pkuznetsov.recipes.dao

import cats.effect.IO
import doobie.util.transactor.Transactor
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.ExecutionContext

class PostgresqlRecipeQuiresTest extends FunSuite with Matchers with doobie.scalatest.IOChecker {
  implicit val cs = IO.contextShift(ExecutionContext.global)

  override def transactor = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:recipes",
    "pavel",
    "password"
  )

  test("create ingredients table") {
    check(PostgresqlRecipeQuires.createIngredientsTable)
  }

  test("create ingredient names table") {
    check(PostgresqlRecipeQuires.createIngredientNamesTable)
  }

  test("create recipes table") {
    check(PostgresqlRecipeQuires.createRecipesTable)
  }

  test("insert recipe") {
    check(
      PostgresqlRecipeQuires.insertRecipe(
        RecipeRow(
          0,
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
        ))
    )
  }

  test("select recipe") {
    check(PostgresqlRecipeQuires.selectRecipe(10))
  }

  test("insert ingredient") {
    check(PostgresqlRecipeQuires.insertIngredient(IngredientRow(10, 100, 123.23, Some("ml"))))
  }

  test("select ingredient") {
    check(PostgresqlRecipeQuires.selectIngredient(10))
  }

  test("insert ingredient name") {
    check(PostgresqlRecipeQuires.insertIngredientName("banana"))
  }

  test("select ingredient name") {
    check(PostgresqlRecipeQuires.selectIngredientNameId("banana"))
  }

}
