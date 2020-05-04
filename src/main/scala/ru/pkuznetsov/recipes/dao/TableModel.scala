package ru.pkuznetsov.recipes.dao

case class RecipeRow(recipeId: Int,
                     name: String,
                     uri: Option[String],
                     summary: String,
                     author: String,
                     cookingTime: Option[Int],
                     calories: Option[Double],
                     protein: Option[Double],
                     fat: Option[Double],
                     carbohydrates: Option[Double],
                     sugar: Option[Double])

case class IngredientRow(recipeId: Int, ingredientId: Int, amount: Double, `unit`: Option[String])
