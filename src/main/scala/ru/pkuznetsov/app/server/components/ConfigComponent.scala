package ru.pkuznetsov.app.server.components

import cats.effect.{Resource, Sync}
import pureconfig.ConfigSource
import pureconfig.generic.auto._

case class ConfigComponent(
    psqlUser: String,
    psqlPassword: String,
    dbUrl: String,
    dbThreads: Int,
    appThreads: Int,
    appHost: String,
    appPort: Int
)

object ConfigComponent {
  def apply[F[_]: Sync](): Resource[F, ConfigComponent] = {
    Resource.liftF[F, ConfigComponent](
      Sync[F].delay(ConfigSource.resources("application-server.conf").loadOrThrow[ConfigComponent]))
  }
}
