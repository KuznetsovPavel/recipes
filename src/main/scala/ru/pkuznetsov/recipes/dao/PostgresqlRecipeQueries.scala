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

  def selectRecipesByIngredients(ids: NonEmptyList[Int]) =
    (fr"""SELECT DISTINCT i1.recipeId FROM ingredients i1 LEFT JOIN (
           SELECT recipeId FROM ingredients where """ ++ Fragments.notIn(fr"ingredientId", ids) ++
      fr") i2 on (i1.recipeId = i2.recipeId) where i2.recipeId is NULL").query[Int]
}
