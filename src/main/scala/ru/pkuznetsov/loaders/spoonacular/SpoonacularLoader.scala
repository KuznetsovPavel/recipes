package ru.pkuznetsov.loaders.spoonacular

import cats.MonadError
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.monadError._
import com.typesafe.scalalogging.StrictLogging
import io.circe.Json
import ru.pkuznetsov.core.model.AppError.SpoonacularError
import ru.pkuznetsov.loaders.spoonacular.SpoonacularLoader.{Backend, SpoonacularApiKey, SpoonacularRecipeId}
import ru.pkuznetsov.recipes.model.Recipe
import sttp.client._
import sttp.client.circe._
import supertagged.TaggedType

class SpoonacularLoader[F[_]](backend: Backend[F], apiKey: SpoonacularApiKey)(
    implicit monad: MonadError[F, Throwable])
    extends StrictLogging {

  def getRecipe(recipeId: SpoonacularRecipeId): F[Recipe] = {
    val request = basicRequest
      .get(uri"https://api.spoonacular.com/recipes/$recipeId/information?apiKey=$apiKey")
      .response(asJson[Json])

    for {
      response <- backend.send(request)
      recipeJson <- monad
        .fromEither(response.body)
        .adaptError { case ex => SpoonacularError(ex) }
      _ <- monad.pure(logger.debug(s"get recipe with id $recipeId, content: $recipeJson"))
      recipe <- SpoonacularDeserializator.fromResponse[F](recipeJson)
    } yield recipe

  }
}

object SpoonacularLoader {
  type Backend[F[_]] = SttpBackend[F, Nothing, Nothing]
  object SpoonacularRecipeId extends TaggedType[Int]
  type SpoonacularRecipeId = SpoonacularRecipeId.Type

  object SpoonacularApiKey extends TaggedType[String]
  type SpoonacularApiKey = SpoonacularApiKey.Type
}
