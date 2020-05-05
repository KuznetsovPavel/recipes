package ru.pkuznetsov.recipes.services

import ru.pkuznetsov.recipes.dao.PostgresqlRecipeDao
import ru.pkuznetsov.recipes.model.Recipe

trait RecipeService[F[_]] {
  def save(recipe: Recipe): F[Int]
  def get(id: Int): F[Recipe]
}

class RecipeServiceImpl[F[_]](dao: PostgresqlRecipeDao[F]) extends RecipeService[F] {
  def save(recipe: Recipe): F[Int] = dao.insertRecipe(recipe)
  def get(id: Int): F[Recipe] = dao.selectRecipe(id)
}
