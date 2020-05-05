package ru.pkuznetsov.recipes.dao

import cats.MonadError
import cats.effect.{Bracket, Resource}
import cats.free.Free
import cats.instances.list._
import cats.syntax.flatMap._
import cats.syntax.traverse._
import doobie.free.connection
import doobie.hikari.HikariTransactor
import doobie.implicits.toConnectionIOOps
import ru.pkuznetsov.recipes.model.{Ingredient, Recipe}

import scala.language.implicitConversions

class PostgresqlRecipeDao[F[_]](transactor: Resource[F, HikariTransactor[F]], manager: RecipeTableManager[F])(
    implicit bracket: Bracket[F, Throwable],
    monad: MonadError[F, Throwable]
) {

  implicit def connectionIO2F[A](transaction: Free[connection.ConnectionOp, A]): F[A] =
    transactor.use(transactor => transaction.transact(transactor))

  def createTables: F[Unit] =
    for {
      _ <- PostgresqlRecipeQueries.createRecipesTable.run
      _ <- PostgresqlRecipeQueries.createIngredientNamesTable.run
      _ <- PostgresqlRecipeQueries.createIngredientsTable.run
    } yield ()

  def insertRecipe(recipe: Recipe): F[Int] =
    for {
      recipeId <- PostgresqlRecipeQueries
        .insertRecipe(manager.recipe2RecipeRow(recipe))
        .withUniqueGeneratedKeys[Int]("id")
      _ <- recipe.ingredients.traverse(insertIngredient(_, recipeId))
    } yield recipeId

  def selectRecipe(recipeId: Int): F[Recipe] = {
    val fromTable: F[(Option[RecipeRow], List[IngredientRow], Map[Int, String])] = for {
      recipeRowOpt <- PostgresqlRecipeQueries.selectRecipe(recipeId).option
      ingRows <- PostgresqlRecipeQueries.selectIngredient(recipeId).to[List]
      names <- ingRows
        .map(_.ingredientId)
        .traverse(id => PostgresqlRecipeQueries.selectIngredientName(id).option)
    } yield (recipeRowOpt, ingRows, names.flatten.toMap)

    fromTable.flatMap {
      case (recipe, ings, names) => manager.createRecipeFrom(recipeId, recipe, ings, names)
    }
  }

  private def insertIngredient(ingredient: Ingredient, recipeId: Int): Free[connection.ConnectionOp, Int] = {
    def getIngNameId =
      PostgresqlRecipeQueries
        .selectIngredientNameId(ingredient.name)
        .option

    def insertIngName: doobie.ConnectionIO[Int] =
      PostgresqlRecipeQueries
        .insertIngredientName(ingredient.name)
        .withUniqueGeneratedKeys[Int]("id")

    def insertIng(ingNameId: Int) =
      PostgresqlRecipeQueries
        .insertIngredient(IngredientRow(recipeId, ingNameId, ingredient.amount, ingredient.unit))
        .run

    for {
      optIngNameId <- getIngNameId
      result <- optIngNameId match {
        case Some(value) => insertIng(value)
        case None        => insertIngName.flatMap(insertIng)
      }
    } yield result
  }
}
