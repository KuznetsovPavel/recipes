package ru.pkuznetsov.recipes.services

import ru.pkuznetsov.recipes.dao.PostgresqlRecipeDao
import ru.pkuznetsov.recipes.model.Recipe
import ru.pkuznetsov.recipes.services.RecipeService.RecipeId
import supertagged.TaggedType

trait RecipeService[F[_]] {
  def save(recipe: Recipe): F[Int]
  def get(id: RecipeId): F[Recipe]
}

class RecipeServiceImpl[F[_]](dao: PostgresqlRecipeDao[F]) extends RecipeService[F] {
  def save(recipe: Recipe): F[Int] = dao.insertRecipe(recipe)
  def get(id: RecipeId): F[Recipe] = dao.selectRecipe(id)
}

object RecipeService {
  object RecipeId extends TaggedType[Int]
  type RecipeId = RecipeId.Type
}
