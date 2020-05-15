package ru.pkuznetsov.app.server.components

import java.util.concurrent.Executors

import cats.Monad
import cats.effect.{ConcurrentEffect, Resource, Timer}
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.{Router, Server}

import scala.concurrent.ExecutionContext

object ServerComponent {

  def apply[F[_]: Monad: ConcurrentEffect: Timer](config: ConfigComponent,
                                                  routes: HttpRoutes[F]): Resource[F, Server[F]] =
    BlazeServerBuilder[F](
      ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(config.appThreads)))
      .bindHttp(config.appPort, config.appHost)
      .withHttpApp(Router("/" -> routes).orNotFound)
      .resource

}
