package ru.pkuznetsov.recipes.dao

import doobie.implicits._

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
         |  ingredient VARCHAR NOT NULL
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

}
