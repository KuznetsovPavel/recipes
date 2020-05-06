package ru.pkuznetsov.recipes.dao

import cats.MonadError
import cats.effect.{Bracket, Resource}
import cats.free.Free
import cats.instances.list._
import cats.syntax.flatMap._
import cats.syntax.traverse._
import doobie.free.connection
import doobie.hikari.HikariTransactor
import ru.pkuznetsov.core.dao.{CommonPostgresQueries, Dao}
import ru.pkuznetsov.core.model.Ingredient
import ru.pkuznetsov.recipes.model.Recipe
import ru.pkuznetsov.recipes.services.RecipeService.RecipeId

class PostgresqlRecipeDao[F[_]](transactor: Resource[F, HikariTransactor[F]], manager: RecipeTableManager[F])(
    implicit bracket: Bracket[F, Throwable],
    monad: MonadError[F, Throwable]
) extends Dao[F](transactor) {

  def insertRecipe(recipe: Recipe): F[Int] =
    for {
      recipeId <- PostgresqlRecipeQueries
        .insertRecipe(manager.recipe2RecipeRow(recipe))
        .withUniqueGeneratedKeys[Int]("id")
      _ <- recipe.ingredients.traverse(insertIngredient(_, recipeId))
    } yield recipeId

  def selectRecipe(recipeId: RecipeId): F[Recipe] = {
    val fromTable: F[(Option[RecipeRow], List[IngredientRow], Map[Int, String])] = for {
      recipeRowOpt <- PostgresqlRecipeQueries.selectRecipe(recipeId).option
      ingRows <- PostgresqlRecipeQueries.selectIngredient(recipeId).to[List]
      names <- ingRows
        .map(_.ingredientId)
        .traverse(id => CommonPostgresQueries.selectIngredientName(id).option)
    } yield (recipeRowOpt, ingRows, names.flatten.toMap)

    fromTable.flatMap {
      case (recipe, ings, names) => manager.createRecipeFrom(recipeId, recipe, ings, names)
    }
  }

  private def insertIngredient(ingredient: Ingredient, recipeId: Int): Free[connection.ConnectionOp, Int] = {
    def insertIng(ingNameId: Int) =
      PostgresqlRecipeQueries
        .insertIngredient(IngredientRow(recipeId, ingNameId, ingredient.amount, ingredient.unit))
        .run

    for {
      optIngNameId <- getIngNameId(ingredient.name)
      result <- optIngNameId match {
        case Some(value) => insertIng(value)
        case None        => insertIngName(ingredient.name).flatMap(insertIng)
      }
    } yield result
  }
}
