package ru.pkuznetsov.recipes.dao

import doobie.implicits._

object PostgresqlRecipeQuires {

  def createRecipesTable: doobie.Update0 =
    sql"""
         |CREATE TABLE IF NOT EXISTS recipes (
         |  id SERIAL PRIMARY KEY,
         |  name VARCHAR NOT NULL,
         |  uri VARCHAR,
         |  summary TEXT NOT NULL,
         |  author VARCHAR NOT NULL,
         |  cookingTime INT,
         |  calories FLOAT,
         |  protein FLOAT,
         |  fat FLOAT,
         |  carbohydrates FLOAT,
         |  sugar FLOAT
         |);
         |""".stripMargin.update

  def createIngredientNamesTable =
    sql"""
         |CREATE TABLE IF NOT EXISTS ingredient_names (
         |  id SERIAL PRIMARY KEY,
         |  ingredient VARCHAR UNIQUE NOT NULL
         |);
         |""".stripMargin.update

  def createIngredientsTable =
    sql"""
         |CREATE TABLE IF NOT EXISTS ingredients (
         |  recipeId SERIAL REFERENCES recipes (id),
         |  ingredientId SERIAL REFERENCES ingredient_names (id),
         |  amount FLOAT NOT NULL,
         |  unit VARCHAR
         |);
         |""".stripMargin.update

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

  def selectIngredient(recipeId: Int) =
    sql"""
         |SELECT recipeId, ingredientId, amount, unit
         |FROM ingredients
         |WHERE recipeId = ${recipeId}
         |""".stripMargin.query[IngredientRow]

  def insertIngredientName(name: String) =
    sql"""
         |INSERT INTO ingredient_names (ingredient)
         |VALUES ($name)
         |""".stripMargin.update

  def selectIngredientNameId(name: String) = {
    sql"""
         |SELECT id FROM ingredient_names
         |WHERE  ingredient = ${name}
         |""".stripMargin.query[Int]
  }

  def selectIngredientName(id: Int) = {
    sql"""
         |SELECT id, ingredient FROM ingredient_names
         |WHERE  id = ${id}
         |""".stripMargin.query[(Int, String)]
  }

}
