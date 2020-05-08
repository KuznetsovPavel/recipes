package ru.pkuznetsov.recipes.dao

import cats.MonadError
import cats.effect.{Bracket, Resource}
import cats.free.Free
import cats.instances.list._
import cats.syntax.traverse._
import doobie.free.connection
import doobie.hikari.HikariTransactor
import ru.pkuznetsov.core.dao.Dao
import ru.pkuznetsov.recipes.services.RecipeService.RecipeId

trait RecipeDao[F[_]] {
  def saveRecipeWithIngredients(recipe: RecipeRow, ingredients: List[IngredientRow]): F[Int]
  def getRecipe(recipeId: RecipeId): F[Option[RecipeRow]]
  def getIngredientForRecipe(recipeId: RecipeId): F[List[IngredientRow]]
  def saveIngredient(ingredient: IngredientRow, recipeId: Int): Free[connection.ConnectionOp, Int]
}

class PostgresqlRecipeDao[F[_]](transactor: Resource[F, HikariTransactor[F]])(
    implicit bracket: Bracket[F, Throwable],
    monad: MonadError[F, Throwable]
) extends Dao[F](transactor)
    with RecipeDao[F] {

  def saveRecipeWithIngredients(recipe: RecipeRow, ingredients: List[IngredientRow]): F[Int] =
    for {
      recipeId <- PostgresqlRecipeQueries
        .insertRecipe(recipe)
        .withUniqueGeneratedKeys[Int]("id")
      _ <- ingredients.traverse(saveIngredient(_, recipeId))
    } yield recipeId

  def getRecipe(recipeId: RecipeId): F[Option[RecipeRow]] =
    PostgresqlRecipeQueries.selectRecipe(recipeId).option

  def getIngredientForRecipe(recipeId: RecipeId): F[List[IngredientRow]] =
    PostgresqlRecipeQueries.selectIngredients(recipeId).to[List]

  def saveIngredient(ingredient: IngredientRow, recipeId: Int): Free[connection.ConnectionOp, Int] =
    PostgresqlRecipeQueries
      .insertIngredient(IngredientRow(recipeId, ingredient.ingredientId, ingredient.amount, ingredient.unit))
      .run

}
