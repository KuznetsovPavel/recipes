package ru.pkuznetsov.loaders.connectors.spoonacular

import cats.MonadError
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.monadError._
import cats.syntax.applicativeError._
import com.typesafe.scalalogging.StrictLogging
import io.circe.Json
import ru.pkuznetsov.loaders.connectors.spoonacular.SpoonacularLoader.{
  Backend,
  LoaderRecipeId,
  SpoonacularApiKey
}
import ru.pkuznetsov.loaders.model.LoaderError
import ru.pkuznetsov.recipes.model.Recipe
import sttp.client._
import sttp.client.circe._
import sttp.model.StatusCode
import supertagged.TaggedType

trait RecipeLoader[F[_]] {
  def getRecipe(recipeId: LoaderRecipeId): F[Recipe]
}

class SpoonacularLoader[F[_]](backend: Backend[F], apiKey: SpoonacularApiKey)(
    implicit monad: MonadError[F, Throwable])
    extends RecipeLoader[F]
    with StrictLogging {

  def getRecipe(recipeId: LoaderRecipeId): F[Recipe] = {
    val request = basicRequest
      .get(uri"https://api.spoonacular.com/recipes/$recipeId/information?apiKey=$apiKey")
      .response(asJson[Json])

    for {
      response <- backend.send(request)
      _ <- response.code match {
        case StatusCode.NotFound =>
          logger.debug(s"recipe with id $recipeId not exist")
          monad.raiseError[Unit](LoaderError.SpoonacularRecipeNotFound(recipeId))
        case _ => monad.pure(())
      }
      recipeJson <- monad.fromEither(response.body).adaptError { ex =>
        logger.debug(s"cannot load recipe with id $recipeId because $ex")
        LoaderError.SpoonacularLoaderError(recipeId)
      }
      _ <- monad.pure(logger.debug(s"get recipe with id $recipeId"))
      recipe <- SpoonacularDeserializator.fromResponse[F](recipeJson)
    } yield recipe

  }
}

object SpoonacularLoader {
  type Backend[F[_]] = SttpBackend[F, Nothing, Nothing]
  object LoaderRecipeId extends TaggedType[Int]
  type LoaderRecipeId = LoaderRecipeId.Type

  object SpoonacularApiKey extends TaggedType[String]
  type SpoonacularApiKey = SpoonacularApiKey.Type
}
