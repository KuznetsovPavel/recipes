package ru.pkuznetsov.recipes.api

import cats.data.Nested
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.monadError._
import cats.{Applicative, Defer, MonadError}
import com.typesafe.scalalogging.StrictLogging
import io.circe.Json
import io.circe.syntax._
import org.http4s.{HttpRoutes, Request, Response}
import ru.pkuznetsov.core.api.Http4sController
import ru.pkuznetsov.recipes.model.RecipeError.IncorrectRecipeId
import ru.pkuznetsov.recipes.services.RecipeService
import ru.pkuznetsov.recipes.services.RecipeService.RecipeId

class RecipeController[F[_]: Applicative: Defer: Sync](service: RecipeService[F])(
    implicit monad: MonadError[F, Throwable])
    extends Http4sController[F]
    with StrictLogging {

  override val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case recipe @ POST -> Root => saveRecipe(recipe)
    case GET -> Root / id      => getRecipeById(id)
    case GET -> Root           => getRecipeByBucket
  }

  private def getRecipeById(id: String): F[Response[F]] = {
    val res = for {
      recipeId <- monad.catchNonFatal(id.toInt).adaptError(_ => IncorrectRecipeId(id))
      recipe <- service.get(RecipeId(recipeId))
    } yield recipe.asJson
    checkErrorAndReturn(res)
  }

  private def saveRecipe(req: Request[F]): F[Response[F]] = {
    val res = for {
      recipe <- req.as[RecipeRequestBody]
      response <- service.save(recipe)
    } yield Json.obj(("id", Json.fromInt(response)))
    checkErrorAndReturn(res)
  }

  private def getRecipeByBucket: F[Response[F]] = {
    val res = for {
      bucket <- service.getByBucket
      _ = bucket.map(_.toInt)
    } yield bucket.map(_.toInt).asJson
    checkErrorAndReturn(res)
  }
}
