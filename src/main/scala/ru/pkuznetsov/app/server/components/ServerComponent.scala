package ru.pkuznetsov.app.server.components

import java.util.concurrent.Executors

import cats.Monad
import cats.effect.{ConcurrentEffect, Resource, Timer}
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.{Router, Server}
import ru.pkuznetsov.app.server.components.ConfigComponent.AppConfig

import scala.concurrent.ExecutionContext

object ServerComponent {

  def apply[F[_]: Monad: ConcurrentEffect: Timer](config: AppConfig,
                                                  routes: HttpRoutes[F]): Resource[F, Server[F]] =
    BlazeServerBuilder[F](
      ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(config.appThreads)))
      .bindHttp(config.server.port, config.server.host)
      .withHttpApp(Router("/" -> routes).orNotFound)
      .resource

}
