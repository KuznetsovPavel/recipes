package ru.pkuznetsov.recipes.services

import java.net.URI

import cats.MonadError
import cats.data.NonEmptyList
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.typesafe.scalalogging.StrictLogging
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import ru.pkuznetsov.bucket.dao.BucketDao
import ru.pkuznetsov.bucket.model.Bucket
import ru.pkuznetsov.ingredients.services.IngredientNameManager
import ru.pkuznetsov.recipes.api.{RecipeRequestBody, RecipeResponseBody}
import ru.pkuznetsov.recipes.dao.RecipeDao
import ru.pkuznetsov.recipes.model.RecipeError.{
  BucketNotExist,
  CannotParseURI,
  EmptyBucket,
  IncorrectMissingIngredients,
  RecipeNotExist
}
import ru.pkuznetsov.recipes.services.RecipeService.{IngredientId, RecipeId}
import supertagged.TaggedType

import scala.util.{Failure, Try}

trait RecipeService[F[_]] {
  def save(recipe: RecipeRequestBody): F[SaveResponse]
  def get(id: RecipeId): F[RecipeResponseBody]
  def delete(id: RecipeId): F[Unit]
  def getByBucket: F[List[RecipeId]]
  def getByPartOfIngredients(missingCount: Int): F[List[RecipeId]]
}

final case class SaveResponse(id: Int)
object SaveResponse {
  implicit val decoder: Decoder[SaveResponse] = deriveDecoder[SaveResponse]
  implicit val encoder: Encoder[SaveResponse] = deriveEncoder[SaveResponse]
}

class RecipeServiceImpl[F[_]: Sync](
    recipeDao: RecipeDao[F],
    bucketDao: BucketDao[F],
    ingredientNameManager: IngredientNameManager[F],
    recipeTableManager: RecipeTableManager[F])(implicit monad: MonadError[F, Throwable])
    extends RecipeService[F]
    with StrictLogging {

  def save(recipe: RecipeRequestBody): F[SaveResponse] =
    for {
      _ <- Sync[F].delay(logger.debug(s"saving recipe $recipe"))
      _ <- recipe.uri.map(uri => Try(URI.create(uri))) match {
        case Some(Failure(_)) => monad.raiseError[Unit](CannotParseURI(recipe.uri.get, RecipeId(0)))
        case _                => monad.pure(())
      }
      ingredientIds <- monad.pure(recipe.ingredients.map(ing => IngredientId(ing.id)))
      _ <- ingredientNameManager.checkIngredientIds(ingredientIds)
      _ <- Sync[F].delay(logger.debug(s"all ingredient names is correct for: ${ingredientIds.mkString}"))
      recipeRow <- monad.pure(recipeTableManager.recipeRequest2RecipeRow(recipe))
      ingRows <- monad.pure(recipe.ingredients.map(recipeTableManager.ingRequest2IngRow(_, RecipeId(0))))
      recipeId <- recipeDao.saveRecipeWithIngredients(recipeRow, ingRows)
      _ <- Sync[F].delay(logger.debug(s"recipe ${recipe} was saved with id ${recipeId}"))
    } yield SaveResponse(recipeId)

  def get(id: RecipeId): F[RecipeResponseBody] =
    for {
      _ <- Sync[F].delay(logger.debug(s"getting recipe by id $id"))
      recipeRowOpt <- recipeDao.getRecipe(id)
      recipeRow <- monad.fromOption(recipeRowOpt, RecipeNotExist(id))
      ingredientRows <- recipeDao.getIngredientForRecipe(id)
      ingredientNames <- ingredientNameManager.getIngredientNamesFor(
        ingredientRows.map(ing => IngredientId(ing.ingredientId)))
      _ <- Sync[F].delay(logger.debug(s"got all data for recipe $id"))
      recipe <- recipeTableManager.createRecipeFrom(recipeRow, ingredientRows, ingredientNames)
      _ <- Sync[F].delay(logger.debug(s"got recipe $id successfully"))
    } yield RecipeResponseBody.fromRecipe(recipe)

  def delete(id: RecipeId): F[Unit] =
    for {
      _ <- Sync[F].delay(logger.debug(s"delete recipe with id $id"))
      rows <- recipeDao.deleteRecipe(id)
      _ <- if (rows == 0) monad.raiseError[Unit](RecipeNotExist(id)) else monad.unit
      _ <- Sync[F].delay(logger.debug(s"recipe with id $id was delete successfully, $rows rows were deleted"))
    } yield ()

  def getByBucket: F[List[RecipeId]] =
    for {
      _ <- Sync[F].delay(logger.debug(s"getting recipes by bucket"))
      ingredients <- getIngredientsFromBucket
      recipes <- recipeDao.getRecipesByIngredients(ingredients)
      _ <- Sync[F].delay(logger.debug(s"got recipes by bucket successfully"))
    } yield recipes

  def getByPartOfIngredients(missingCount: Int): F[List[RecipeId]] =
    for {
      _ <- Sync[F].delay(logger.debug(s"getting recipes by part of ingredients"))
      _ <- if (missingCount <= 0) monad.raiseError[Unit](IncorrectMissingIngredients) else monad.unit
      ingredients <- getIngredientsFromBucket
      recipes <- recipeDao.getRecipesByPartIngredients(ingredients, missingCount)
      _ <- Sync[F].delay(logger.debug(s"got recipes by part if bucket successfully"))
    } yield recipes.sortBy(_.intValue)

  private def getIngredientsFromBucket: F[NonEmptyList[IngredientId]] =
    for {
      bucketOpt <- bucketDao.getBucket
      ingredients <- bucketOpt match {
        case Some(Bucket(Nil)) => monad.raiseError[NonEmptyList[IngredientId]](EmptyBucket)
        case None              => monad.raiseError[NonEmptyList[IngredientId]](BucketNotExist)
        case Some(Bucket(x :: xs)) =>
          monad.pure(NonEmptyList.of(x, xs: _*)).map(_.map(ing => IngredientId(ing.ingredientId)))
      }
      _ <- Sync[F].delay(logger.debug(s"get ingredients ${ingredients.toList.mkString(", ")} from bucket"))
    } yield ingredients

}

object RecipeService {
  object RecipeId extends TaggedType[Int]
  type RecipeId = RecipeId.Type

  object IngredientId extends TaggedType[Int]
  type IngredientId = IngredientId.Type
}
