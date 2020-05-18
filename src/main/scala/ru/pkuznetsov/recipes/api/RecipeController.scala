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

  val deleteRecipe = endpoint.delete
    .in("recipe" / path[Int])
    .errorOut(jsonBody[ErrorData])

  val getRecipeByBucket = endpoint.get
    .in("recipe")
    .in("byBucket")
    .out(jsonBody[List[Int]])
    .errorOut(jsonBody[ErrorData])

  val getRecipeByPartOfBucket = endpoint.get
    .in("recipe")
    .in("missingIngredients" / path[Int])
    .out(jsonBody[List[Int]])
    .errorOut(jsonBody[ErrorData])

  val saveRecipeRoutes = saveRecipe.toRoutes(rrb => service.save(rrb).toTapirResponse)

  val getRecipeRoutes = getRecipe.toRoutes(id => service.get(RecipeId(id)).toTapirResponse)

  val getRecipeByBucketRoutes =
    getRecipeByBucket.toRoutes(_ => service.getByBucket.map(_.map(_.intValue)).toTapirResponse)

  val getRecipeByPartOfBucketRoutes =
    getRecipeByPartOfBucket.toRoutes(missing =>
      service.getByPartOfIngredients(missing).map(_.map(_.intValue)).toTapirResponse)

  val deleteRecipeRoutes = deleteRecipe.toRoutes(id => service.delete(RecipeId(id)).toTapirResponse)

  override val endpoints =
    List(saveRecipe, getRecipe, getRecipeByBucket, deleteRecipe, getRecipeByPartOfBucket)

  override val routes = saveRecipeRoutes <+> getRecipeRoutes <+> getRecipeByBucketRoutes <+> deleteRecipeRoutes <+> getRecipeByPartOfBucketRoutes
}
