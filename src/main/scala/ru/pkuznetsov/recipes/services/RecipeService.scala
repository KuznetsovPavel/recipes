package ru.pkuznetsov.recipes.services

import cats.MonadError
import cats.data.{Nested, NonEmptyList}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.typesafe.scalalogging.StrictLogging
import ru.pkuznetsov.bucket.dao.BucketDao
import ru.pkuznetsov.bucket.model.Bucket
import ru.pkuznetsov.ingredients.services.IngredientNameManager
import ru.pkuznetsov.recipes.api.RecipeRequestBody
import ru.pkuznetsov.recipes.dao.RecipeDao
import ru.pkuznetsov.recipes.model.Recipe
import ru.pkuznetsov.recipes.model.RecipeError.{BucketNotExist, EmptyBucket, RecipeNotExist}
import ru.pkuznetsov.recipes.services.RecipeService.{IngredientId, RecipeId}
import supertagged.TaggedType

trait RecipeService[F[_]] {
  def save(recipe: RecipeRequestBody): F[RecipeId]
  def get(id: RecipeId): F[Recipe]
  def getByBucket: F[List[RecipeId]]
}

class RecipeServiceImpl[F[_]](
    recipeDao: RecipeDao[F],
    bucketDao: BucketDao[F],
    ingredientNameManager: IngredientNameManager[F],
    recipeTableManager: RecipeTableManager[F])(implicit monad: MonadError[F, Throwable])
    extends RecipeService[F]
    with StrictLogging {

  def save(recipe: RecipeRequestBody): F[RecipeId] =
    for {
      _ <- monad.pure(logger.debug(s"saving recipe $recipe"))
      ingredientIds <- monad.pure(recipe.ingredients.map(ing => IngredientId(ing.id)))
      _ <- ingredientNameManager.checkIngredientIds(ingredientIds)
      _ <- monad.pure(logger.debug(s"all ingredient names is correct for: ${ingredientIds.mkString}"))
      recipeRow <- monad.pure(recipeTableManager.recipeRequest2RecipeRow(recipe))
      ingRows <- monad.pure(recipe.ingredients.map(recipeTableManager.ingRequest2IngRow(_, RecipeId(0))))
      recipeId <- recipeDao.saveRecipeWithIngredients(recipeRow, ingRows)
      _ <- monad.pure(logger.debug(s"recipe ${recipe} was saved with id ${recipeId}"))
    } yield recipeId

  def get(id: RecipeId): F[Recipe] =
    for {
      _ <- monad.pure(logger.debug(s"getting recipe by id $id"))
      recipeRowOpt <- recipeDao.getRecipe(id)
      recipeRow <- monad.fromOption(recipeRowOpt, RecipeNotExist(id))
      ingredientRows <- recipeDao.getIngredientForRecipe(id)
      ingredientNames <- ingredientNameManager.getIngredientNamesFor(
        ingredientRows.map(ing => IngredientId(ing.ingredientId)))
      _ <- monad.pure(logger.debug(s"got all data for recipe $id"))
      recipe <- recipeTableManager.createRecipeFrom(recipeRow, ingredientRows, ingredientNames)
      _ <- monad.pure(logger.debug(s"got recipe $id successfully"))
    } yield recipe

  def getByBucket: F[List[RecipeId]] =
    for {
      _ <- monad.pure(logger.debug(s"getting recipes by bucket"))
      bucketOpt <- bucketDao.getBucket
      ingredients <- bucketOpt match {
        case Some(Bucket(Nil)) => monad.raiseError[NonEmptyList[IngredientId]](EmptyBucket)
        case None              => monad.raiseError[NonEmptyList[IngredientId]](BucketNotExist)
        case Some(Bucket(x :: xs)) =>
          monad.pure(NonEmptyList.of(x, xs: _*)).map(_.map(ing => IngredientId(ing.ingredientId)))
      }
      _ <- monad.pure(logger.debug(s"get ingredients ${ingredients.toList.mkString(", ")} from bucket"))
      recipes <- recipeDao.getRecipesByIngredients(ingredients)
      _ <- monad.pure(logger.debug(s"got recipes by bucket successfully"))
    } yield recipes

}

object RecipeService {
  object RecipeId extends TaggedType[Int]
  type RecipeId = RecipeId.Type

  object IngredientId extends TaggedType[Int]
  type IngredientId = IngredientId.Type
}
