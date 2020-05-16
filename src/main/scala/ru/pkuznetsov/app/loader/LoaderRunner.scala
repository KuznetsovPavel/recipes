package ru.pkuznetsov.app.loader

import cats.effect.{ExitCode, IO, IOApp}
import ru.pkuznetsov.app.loader.components.{ConfigComponent, SchedulerComponent, ServiceComponent}

object LoaderRunner extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val resources = for {
      config <- ConfigComponent[IO]()
      service <- ServiceComponent[IO](config)
      scheduler <- SchedulerComponent[IO](service, config.spoonacular)
    } yield scheduler

    resources
      .use(scheduler => scheduler.action)
      .map(_ => ExitCode.Success)
  }
}
