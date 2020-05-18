package ru.pkuznetsov.ingredients.dao

import cats.data.NonEmptyList
import org.scalatest.tagobjects.Slow
import ru.pkuznetsov.core.dao.{DbTest, DbTestTag}

class IngredientNamesPostgresQueriesTest extends DbTest {

  test("insert ingredient name", Slow, DbTestTag) {
    check(IngredientNamesPostgresQueries.insert("banana"))
  }

  test("select ingredient name by id", Slow, DbTestTag) {
    check(IngredientNamesPostgresQueries.selectById(10))
  }

  test("select ingredient name by ids", Slow, DbTestTag) {
    check(IngredientNamesPostgresQueries.selectByIds(NonEmptyList.of(1, 2, 3, 4)))
  }

  test("select ingredient name by names", Slow, DbTestTag) {
    check(IngredientNamesPostgresQueries.selectByNames(NonEmptyList.of("one", "two", "three")))
  }

}
