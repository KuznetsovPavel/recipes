package ru.pkuznetsov.app.server.components

import cats.effect.{Resource, Sync}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import ru.pkuznetsov.app.utill.PostgresConfig

object ConfigComponent {

  final case class Server(host: String, port: Int)

  final case class AppConfig(
      database: PostgresConfig,
      appThreads: Int,
      server: Server
  )

  def apply[F[_]: Sync](): Resource[F, AppConfig] = {
    Resource.liftF[F, AppConfig](
      Sync[F].delay(ConfigSource.resources("application-server.conf").loadOrThrow[AppConfig]))
  }
}
