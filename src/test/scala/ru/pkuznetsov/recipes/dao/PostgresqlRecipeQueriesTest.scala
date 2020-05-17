package ru.pkuznetsov.recipes.dao

import cats.data.NonEmptyList
import org.scalatest.tagobjects.Slow
import ru.pkuznetsov.core.dao.{DbTest, DbTestTag}

class PostgresqlRecipeQueriesTest extends DbTest {

  test("insert recipe", Slow, DbTestTag) {
    check(
      PostgresqlRecipeQueries.insertRecipe(
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

  test("select recipe", Slow, DbTestTag) {
    check(PostgresqlRecipeQueries.selectRecipe(10))
  }

  test("insert ingredient", Slow, DbTestTag) {
    check(PostgresqlRecipeQueries.insertIngredient(IngredientRow(10, 100, 123.23, Some("ml"))))
  }

  test("select ingredient", Slow, DbTestTag) {
    check(PostgresqlRecipeQueries.selectIngredients(10))
  }

  test("delete recipe", Slow, DbTestTag) {
    check(PostgresqlRecipeQueries.deleteRecipe(10))
  }

  test("delete ingredients", Slow, DbTestTag) {
    check(PostgresqlRecipeQueries.deleteIngredients(10))
  }

  test("select recipe with ingredients", Slow, DbTestTag) {
    check(PostgresqlRecipeQueries.selectRecipesByIngredients(NonEmptyList.of(1, 2, 3, 4)))
  }

}
