package ru.pkuznetsov.app.loader.components

import cats.effect.{Resource, Sync}
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object ConfigComponent {

  final case class LoaderConfig(psqlUser: String,
                                psqlPassword: String,
                                dbUrl: String,
                                dbThreads: Int,
                                appThreads: Int,
                                spoonacularApiKey: String,
                                loadFrom: Int,
                                quotas: Int)

  def apply[F[_]: Sync](): Resource[F, LoaderConfig] = {
    Resource.liftF[F, LoaderConfig](
      Sync[F].delay(ConfigSource.resources("application-loader.conf").loadOrThrow[LoaderConfig]))
  }

}
