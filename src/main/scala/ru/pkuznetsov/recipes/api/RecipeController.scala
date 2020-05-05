package ru.pkuznetsov.recipes.api

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.monadError._
import cats.{Applicative, Defer, MonadError}
import com.typesafe.scalalogging.StrictLogging
import io.circe.syntax._
import org.http4s.circe.jsonEncoder
import org.http4s.{HttpRoutes, Request, Response}
import ru.pkuznetsov.core.Http4sController
import ru.pkuznetsov.recipes.model.Errors.IncorrectRecipeId
import ru.pkuznetsov.recipes.model.Recipe
import ru.pkuznetsov.recipes.services.RecipeService

class RecipeController[F[_]: Applicative: Defer: Sync](service: RecipeService[F])(
    implicit monad: MonadError[F, Throwable])
    extends Http4sController[F]
    with StrictLogging {

  override val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case recipe @ POST -> Root / "recipes" => saveRecipe(recipe)
    case GET -> Root / "recipes" / id      => getRecipeById(id)
  }

  def getRecipeById(id: String): F[Response[F]] =
    for {
      recipeId <- monad.catchNonFatal(id.toInt).adaptError(_ => IncorrectRecipeId(id))
      recipe <- service.get(recipeId)
      result <- Ok(recipe.asJson)
    } yield result

  def saveRecipe(req: Request[F]): F[Response[F]] =
    for {
      recipe <- req.as[Recipe]
      response <- service.save(recipe)
      result <- Ok(response.toString)
    } yield result

}
