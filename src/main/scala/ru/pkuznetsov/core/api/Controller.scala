package ru.pkuznetsov.core.api

import cats.syntax.applicativeError._
import cats.syntax.either._
import cats.syntax.functor._
import cats.{Applicative, Defer, MonadError}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import ru.pkuznetsov.core.model.{AppError, ErrorData}
import sttp.tapir.Endpoint

trait Controller[F[_]] {
  val endpoints: List[Endpoint[_, _, _, _]]
  val routes: HttpRoutes[F]
}

abstract class Http4sController[F[_]: Applicative: Defer](implicit monad: MonadError[F, Throwable])
    extends Controller[F]
    with Http4sDsl[F] {

  implicit class RichF[A](data: F[A]) {
    def toTapirResponse: F[Either[ErrorData, A]] =
      data
        .map(_.asRight[ErrorData])
        .handleErrorWith(e => monad.pure(AppError.handleError(e).asLeft[A]))
  }

}
