package ru.pkuznetsov.recipes.dao

import cats.data.NonEmptyList
import doobie.Fragments
import doobie.implicits._

object PostgresqlRecipeQueries {

  def insertRecipe(recipeRow: RecipeRow) =
    sql"""
         |INSERT INTO recipes (name, uri, summary, author, cookingTime, calories, protein, fat, carbohydrates, sugar)
         |VALUES (${recipeRow.name}, ${recipeRow.uri}, ${recipeRow.summary}, ${recipeRow.author}, ${recipeRow.cookingTime}, ${recipeRow.calories}, ${recipeRow.protein}, ${recipeRow.fat}, ${recipeRow.carbohydrates}, ${recipeRow.sugar})
         |""".stripMargin.update

  def selectRecipe(id: Int) =
    sql"""
         |SELECT id, name, uri, summary, author, cookingTime, calories, protein, fat, carbohydrates, sugar
         |FROM recipes
         |WHERE id = ${id}
         |""".stripMargin.query[RecipeRow]

  def deleteRecipe(id: Int) =
    sql"""
         |DELETE FROM recipes
         |WHERE id = ${id}
         |""".stripMargin.update

  def insertIngredient(ing: IngredientRow) =
    sql"""
         |INSERT INTO ingredients (recipeId, ingredientId, amount, unit)
         |VALUES (${ing.recipeId}, ${ing.ingredientId}, ${ing.amount}, ${ing.unit})
         |""".stripMargin.update

  def selectIngredients(recipeId: Int) =
    sql"""
         |SELECT recipeId, ingredientId, amount, unit
         |FROM ingredients
         |WHERE recipeId = ${recipeId}
         |""".stripMargin.query[IngredientRow]

  def deleteIngredients(recipeId: Int) =
    sql"""
         |DELETE FROM ingredients
         |WHERE recipeId = ${recipeId}
         |""".stripMargin.update

  def selectRecipesByIngredients(ids: NonEmptyList[Int]) =
    (fr"""SELECT DISTINCT i1.recipeId FROM ingredients i1 LEFT JOIN (
           SELECT recipeId FROM ingredients WHERE """ ++ Fragments.notIn(fr"ingredientId", ids) ++
      fr") i2 ON (i1.recipeId = i2.recipeId) WHERE i2.recipeId IS NULL").query[Int]

  def selectRecipesByPartIngredients(ids: NonEmptyList[Int], missingIngredients: Long) =
    (fr"""SELECT i1.recipeId FROM ingredients i1 LEFT JOIN (
      SELECT recipeId, ingredientId FROM ingredients WHERE """ ++ Fragments.notIn(fr"ingredientId", ids) ++
      fr""") i2 ON (i1.recipeId = i2.recipeId AND i1.ingredientid = i2.ingredientid)
        GROUP BY (i1.recipeid) HAVING count(i2.ingredientid) < $missingIngredients""").query[Int]
}
