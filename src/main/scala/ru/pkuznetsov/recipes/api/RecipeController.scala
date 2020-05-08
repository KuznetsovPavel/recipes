package ru.pkuznetsov.recipes.api

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.monadError._
import cats.{Applicative, Defer, MonadError}
import com.typesafe.scalalogging.StrictLogging
import io.circe.syntax._
import org.http4s.{HttpRoutes, Request, Response}
import ru.pkuznetsov.core.api.Http4sController
import ru.pkuznetsov.core.model.AppError.IncorrectRecipeId
import ru.pkuznetsov.recipes.model.RecipeError
import ru.pkuznetsov.recipes.services.RecipeService
import ru.pkuznetsov.recipes.services.RecipeService.RecipeId

class RecipeController[F[_]: Applicative: Defer: Sync](service: RecipeService[F])(
    implicit monad: MonadError[F, Throwable])
    extends Http4sController[F]
    with StrictLogging {

  override val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case recipe @ POST -> Root => saveRecipe(recipe)
    case GET -> Root / id      => getRecipeById(id)
  }

  def getRecipeById(id: String): F[Response[F]] = {
    val res = for {
      recipeId <- monad.catchNonFatal(id.toInt).adaptError(_ => IncorrectRecipeId(id))
      recipe <- service.get(RecipeId(recipeId))
    } yield recipe.asJson.printWith(printer)
    checkErrorAndReturn(res, handleRecipeError)
  }

  def saveRecipe(req: Request[F]): F[Response[F]] =
    for {
      recipe <- req.as[RecipeRequestBody]
      response <- service.save(recipe)
      result <- Ok(response.toString)
    } yield result

  def handleRecipeError: PartialFunction[Throwable, String] = {
    case RecipeError.CannotFindIngredient(id) => s"Can not find ingredient with id $id"
    case RecipeError.CannotParseURI(_, _)     => "incorrect uri"
    case RecipeError.RecipeNotExist(id)       => s"recipe with id $id not exist"
  }
}
