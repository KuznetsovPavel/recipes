package ru.pkuznetsov.recipes.services

import ru.pkuznetsov.recipes.dao.PostgresqlRecipeDao
import ru.pkuznetsov.recipes.model.Recipe

class RecipeService[F[_]](dao: PostgresqlRecipeDao[F]) {

  def save(recipe: Recipe): F[Int] = dao.insertRecipe(recipe)

}
