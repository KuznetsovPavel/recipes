package ru.pkuznetsov.recipes.dao

import cats.MonadError
import cats.data.NonEmptyList
import cats.effect.{Bracket, Resource}
import cats.instances.list._
import cats.syntax.traverse._
import doobie.hikari.HikariTransactor
import ru.pkuznetsov.core.dao.Dao
import ru.pkuznetsov.recipes.services.RecipeService.{IngredientId, RecipeId}

trait RecipeDao[F[_]] {
  def saveRecipeWithIngredients(recipe: RecipeRow, ingredients: List[IngredientRow]): F[RecipeId]
  def getRecipe(recipeId: RecipeId): F[Option[RecipeRow]]
  def getIngredientForRecipe(recipeId: RecipeId): F[List[IngredientRow]]
  def getRecipesByIngredients(ids: NonEmptyList[IngredientId]): F[List[RecipeId]]
}

class PostgresqlRecipeDao[F[_]](transactor: Resource[F, HikariTransactor[F]])(
    implicit bracket: Bracket[F, Throwable],
    monad: MonadError[F, Throwable]
) extends Dao[F](transactor)
    with RecipeDao[F] {

  def saveRecipeWithIngredients(recipe: RecipeRow, ingredients: List[IngredientRow]): F[RecipeId] =
    for {
      recipeId <- PostgresqlRecipeQueries
        .insertRecipe(recipe)
        .withUniqueGeneratedKeys[Int]("id")
      _ <- ingredients.traverse(saveIngredient(_, recipeId))
    } yield RecipeId(recipeId)

  def getRecipe(recipeId: RecipeId): F[Option[RecipeRow]] =
    PostgresqlRecipeQueries.selectRecipe(recipeId).option

  def getIngredientForRecipe(recipeId: RecipeId): F[List[IngredientRow]] =
    PostgresqlRecipeQueries.selectIngredients(recipeId).to[List]

  private def saveIngredient(ingredient: IngredientRow, recipeId: Int) =
    PostgresqlRecipeQueries
      .insertIngredient(IngredientRow(recipeId, ingredient.ingredientId, ingredient.amount, ingredient.unit))
      .run

  def getRecipesByIngredients(ids: NonEmptyList[IngredientId]) =
    PostgresqlRecipeQueries
      .selectRecipesByIngredients(ids)
      .to[List]
      .map(_.map(id => RecipeId(id)))
}
