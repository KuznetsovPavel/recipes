package ru.pkuznetsov.app.loader.components

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import cats.effect.{Resource, Sync}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import ru.pkuznetsov.app.utill.PostgresConfig
import pureconfig.configurable._

object ConfigComponent {

  final case class SpoonacularConfig(apiKey: String,
                                     loadFrom: Int,
                                     quota: Int,
                                     expireDate: LocalDateTime,
                                     minutesOfSleep: Int)

  final case class LoaderConfig(database: PostgresConfig, spoonacular: SpoonacularConfig)

  implicit val localDateConvert = localDateTimeConfigConvert(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

  def apply[F[_]: Sync](): Resource[F, LoaderConfig] = {
    Resource.liftF[F, LoaderConfig](
      Sync[F].delay(ConfigSource.resources("application-loader.conf").loadOrThrow[LoaderConfig]))
  }
}
