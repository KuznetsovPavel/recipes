package ru.pkuznetsov.recipes.dao

import doobie.implicits._

object PostgresqlRecipeQueries {

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

  def insertRecipe(name: String,
                   uri: Option[String],
                   summary: String,
                   author: String,
                   cookingTime: Option[Int],
                   calories: Option[Double],
                   protein: Option[Double],
                   fat: Option[Double],
                   carbohydrates: Option[Double],
                   sugar: Option[Double]) =
    sql"""
         |INSERT INTO recipes (name, uri, summary, author, cookingTime, calories, protein, fat, carbohydrates, sugar)
         |VALUES ($name, $uri, $summary, $author, $cookingTime, $calories, $protein, $fat, $carbohydrates, $sugar)
         |""".stripMargin.update

  def insertIngredient(recipeId: Int, ingredientId: Int, amount: Double, `unit`: Option[String]) =
    sql"""
         |INSERT INTO ingredients (recipeId, ingredientId, amount, unit)
         |VALUES ($recipeId, $ingredientId, $amount, $unit)
         |""".stripMargin.update

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

}
