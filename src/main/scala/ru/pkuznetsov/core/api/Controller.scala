package ru.pkuznetsov.core.api

import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Applicative, Defer, MonadError}
import io.circe.Json
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import ru.pkuznetsov.core.model.AppError

trait Controller[F[_]] {
  val routes: HttpRoutes[F]
}

abstract class Http4sController[F[_]: Applicative: Defer](implicit monad: MonadError[F, Throwable])
    extends Controller[F]
    with Http4sDsl[F] {

  def checkErrorAndReturn(data: F[Json]) =
    data
      .recover(AppError.handleError andThen errorToJson _)
      .map(json => json.deepDropNullValues)
      .flatMap(res => Ok(res))

  private def errorToJson(str: String): Json = Json.obj(("error", Json.fromString(str)))
}
