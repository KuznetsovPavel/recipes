package ru.pkuznetsov.recipes.api

import cats.effect.{ContextShift, Sync}
import cats.syntax.functor._
import cats.syntax.semigroupk._
import cats.{Applicative, Defer, MonadError}
import com.typesafe.scalalogging.StrictLogging
import ru.pkuznetsov.core.api.Http4sController
import ru.pkuznetsov.core.api.TapirJsonCirceImpl._
import ru.pkuznetsov.core.model.ErrorData
import ru.pkuznetsov.recipes.services.RecipeService.RecipeId
import ru.pkuznetsov.recipes.services.{RecipeService, SaveResponse}
import sttp.tapir.server.http4s._
import sttp.tapir.{endpoint, path, stringToPath}

class RecipeController[F[_]: Applicative: Defer: Sync](service: RecipeService[F])(
    implicit monad: MonadError[F, Throwable],
    cs: ContextShift[F])
    extends Http4sController[F]
    with StrictLogging {

  val saveRecipe = endpoint.post
    .in("recipe")
    .in(jsonBody[RecipeRequestBody])
    .out(jsonBody[SaveResponse])
    .errorOut(jsonBody[ErrorData])

  val getRecipe = endpoint.get
    .in("recipe" / path[Int])
    .out(jsonBody[RecipeResponseBody])
    .errorOut(jsonBody[ErrorData])

  val getRecipeByBucket = endpoint.get
    .in("recipe")
    .in("byBucket")
    .out(jsonBody[List[Int]])
    .errorOut(jsonBody[ErrorData])

  val saveRecipeRoutes = saveRecipe.toRoutes(rrb => service.save(rrb).toTapirResponse)

  val getRecipeRoutes = getRecipe.toRoutes(id => service.get(RecipeId(id)).toTapirResponse)

  val getRecipeByBucketRoutes =
    getRecipeByBucket.toRoutes(_ => service.getByBucket.map(_.map(_.intValue)).toTapirResponse)

  override val endpoints = List(saveRecipe, getRecipe, getRecipeByBucket)

  override val routes = saveRecipeRoutes <+> getRecipeRoutes <+> getRecipeByBucketRoutes
}
