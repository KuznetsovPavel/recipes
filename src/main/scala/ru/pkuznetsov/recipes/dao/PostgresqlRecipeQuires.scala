package ru.pkuznetsov.recipes.dao

import doobie.implicits._
import ru.pkuznetsov.recipes.model.Ingredient

object PostgresqlRecipeQuires {

  val createRecipesTable =
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

  val createIngredientNamesTable =
    sql"""
         |CREATE TABLE IF NOT EXISTS ingredient_names (
         |  id SERIAL PRIMARY KEY,
         |  ingredient VARCHAR UNIQUE NOT NULL
         |);
         |""".stripMargin.update.run

  val createIngredientsTable =
    sql"""
         |CREATE TABLE IF NOT EXISTS ingredients (
         |  recipeId SERIAL REFERENCES recipes (id),
         |  ingredientId SERIAL REFERENCES ingredient_names (id),
         |  amount REAL NOT NULL,
         |  unit VARCHAR NOT NULL
         |);
         |""".stripMargin.update.run

  def insertIngredientIfNotExist(ingredient: Ingredient): doobie.ConnectionIO[Int] = {
   sql"""
              |INSERT INTO ingredient_names (ingredient)
              |VALUES (${ingredient.name})
              |ON CONFLICT (ingredient) DO NOTHING
              |""".stripMargin.update.run
  }

  def getIngredientNameId(ingredient: Ingredient): doobie.Query0[Long] = {
    sql"""
              |SELECT id FROM ingredient_names
              |WHERE  ingredient = ${ingredient.name}
              |""".stripMargin.query[Long]
  }

}
