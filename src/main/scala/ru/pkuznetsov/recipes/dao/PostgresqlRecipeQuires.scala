package ru.pkuznetsov.recipes.dao

import doobie.implicits._

object PostgresqlRecipeQuires {

  val createRecipesTable: doobie.ConnectionIO[Int] =
    sql"""
         |CREATE TABLE IF NOT EXISTS recipes (
         |  id SERIAL PRIMARY KEY,
         |  uri VARCHAR,
         |  summary TEXT NOT NULL,
         |  author VARCHAR NOT NULL,
         |  cookingTime INT,
         |  calories REAL,
         |  protein REAL,
         |  fat REAL,
         |  carbohydrates REAL,
         |  sugar REAL
         |);
         |""".stripMargin.update.run

  val createIngredientNamesTable: doobie.ConnectionIO[Int] =
    sql"""
         |CREATE TABLE IF NOT EXISTS ingredient_names (
         |  id SERIAL PRIMARY KEY,
         |  ingredient VARCHAR UNIQUE NOT NULL
         |);
         |""".stripMargin.update.run

  val createIngredientsTable: doobie.ConnectionIO[Int] =
    sql"""
         |CREATE TABLE IF NOT EXISTS ingredients (
         |  recipeId SERIAL REFERENCES recipes (id),
         |  ingredientId SERIAL REFERENCES ingredient_names (id),
         |  amount REAL NOT NULL,
         |  unit VARCHAR
         |);
         |""".stripMargin.update.run

  def insertRecipe(uri: Option[String],
                   summary: String,
                   author: String,
                   cookingTime: Option[Int],
                   calories: Option[Double],
                   protein: Option[Double],
                   fat: Option[Double],
                   carbohydrates: Option[Double],
                   sugar: Option[Double]) =
    sql"""
         |INSERT INTO recipes (uri, summary, author, cookingTime, calories, protein, fat, carbohydrates, sugar)
         |VALUES ($uri, $summary, $author, $cookingTime, $calories, $protein, $fat, $carbohydrates, $sugar)
         |""".stripMargin.update.withUniqueGeneratedKeys[Int]("id")

  def insertIngredient(recipeId: Long, ingredientId: Long, amount: Double, `unit`: Option[String]) =
    sql"""
         |INSERT INTO ingredients (recipeId, ingredientId, amount, unit)
         |VALUES ($recipeId, $ingredientId, $amount, $unit)
         |""".stripMargin.update.run

  def insertIngredientName(name: String) =
    sql"""
         |INSERT INTO ingredient_names (ingredient)
         |VALUES ($name)
         |""".stripMargin.update.withUniqueGeneratedKeys[Int]("id")

  def selectIngredientNameId(name: String) = {
    sql"""
         |SELECT id FROM ingredient_names
         |WHERE  ingredient = ${name}
         |""".stripMargin.query[Int].option
  }

}
