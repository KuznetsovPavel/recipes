package ru.pkuznetsov.core.api

import cats.{Applicative, Defer}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

trait Controller[F[_]] {
  val routes: HttpRoutes[F]
}

abstract class Http4sController[F[_]: Applicative: Defer] extends Controller[F] with Http4sDsl[F]
