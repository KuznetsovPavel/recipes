package ru.pkuznetsov.app.loader.components

import cats.MonadError
import cats.effect.{Resource, Timer}
import ru.pkuznetsov.app.loader.components.ConfigComponent.SpoonacularConfig
import ru.pkuznetsov.loaders.services.{LoaderService, SchedulerService}

object SchedulerComponent {
  def apply[F[_]: Timer](service: LoaderService[F], config: SpoonacularConfig)(
      implicit monad: MonadError[F, Throwable]): Resource[F, SchedulerService[F]] =
    Resource.pure(new SchedulerService[F](service, config))
}
