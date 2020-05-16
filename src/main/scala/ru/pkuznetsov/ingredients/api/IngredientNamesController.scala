package ru.pkuznetsov.ingredients.api

import cats.effect.{ContextShift, Sync}
import cats.syntax.semigroupk._
import cats.{Applicative, Defer, MonadError}
import org.http4s.HttpRoutes
import ru.pkuznetsov.bucket.model.Bucket
import ru.pkuznetsov.bucket.services.BucketService
import ru.pkuznetsov.core.api.Http4sController
import ru.pkuznetsov.core.api.TapirJsonCirceImpl._
import ru.pkuznetsov.core.model.ErrorData
import ru.pkuznetsov.ingredients.model.{IngredientName, IngredientNameService}
import sttp.tapir.endpoint
import sttp.tapir.server.http4s._

class IngredientNamesController[F[_]: Applicative: Defer: Sync](service: IngredientNameService[F])(
    implicit monad: MonadError[F, Throwable],
    cs: ContextShift[F])
    extends Http4sController[F] {

  val getIngNames = endpoint.get
    .in("ingredients")
    .out(jsonBody[List[IngredientName]])
    .errorOut(jsonBody[ErrorData])

  val getIngNamesRoute = getIngNames.toRoutes(_ => service.getIngredientNames.toTapirResponse)

  override val endpoints = List(getIngNames)

  override val routes: HttpRoutes[F] = getIngNamesRoute

}
