package ru.pkuznetsov.loaders.services

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import cats.MonadError
import cats.effect.Timer
import cats.instances.list._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import ru.pkuznetsov.app.loader.components.ConfigComponent.SpoonacularConfig
import ru.pkuznetsov.loaders.connectors.spoonacular.SpoonacularLoader.LoaderRecipeId
import ru.pkuznetsov.loaders.model.LoaderError

import scala.concurrent.duration.FiniteDuration

class SchedulerService[F[_]: Timer](service: LoaderService[F], config: SpoonacularConfig)(
    implicit monad: MonadError[F, Throwable]) {
  def action: F[Unit] = {
    def handleRecipe(id: Int) = {
      service
        .loadAndSave(LoaderRecipeId(id))
        .map(_ => println(s"recipe was load $id successfully"))
        .handleError(_ => println(s"recipe $id was not load"))
    }

    def sleepAndRun(from: Int) = {
      if (LocalDateTime.now().plusMinutes(config.minutesOfSleep).isAfter(config.expireDate))
        monad.raiseError[Unit](LoaderError.ApiKeyExpired)
      else
        Timer[F]
          .sleep(FiniteDuration(config.minutesOfSleep, TimeUnit.MINUTES))
          .flatMap(_ => iteration(from + config.quota))
    }

    def iteration(from: Int): F[Unit] =
      for {
        _ <- (from until from + config.quota).toList.traverse(handleRecipe)
        _ <- sleepAndRun(from)
      } yield ()

    if (LocalDateTime.now().isAfter(config.expireDate))
      monad.raiseError[Unit](LoaderError.ApiKeyExpired)
    else
      iteration(config.loadFrom)
  }
}
