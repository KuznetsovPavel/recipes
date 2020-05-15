package ru.pkuznetsov.app.loader

import java.util.concurrent.TimeUnit

import cats.effect.{ExitCode, IO, IOApp, Timer}
import cats.instances.list._
import cats.syntax.applicativeError._
import cats.syntax.traverse._
import ru.pkuznetsov.app.loader.components.{ConfigComponent, ServiceComponent}
import ru.pkuznetsov.loaders.connectors.spoonacular.SpoonacularLoader.LoaderRecipeId

import scala.concurrent.duration.FiniteDuration

object LoaderRunner extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val resources = for {
      config <- ConfigComponent[IO]()
      service <- ServiceComponent[IO](config)
    } yield (service, config.loadFrom, config.quotas, 5)

    resources
      .use {
        case (loader, from, quotas, count) => {
          def action(from: Int) = {
            (from until from + quotas).toList
              .traverse { id =>
                loader
                  .loadAndSave(LoaderRecipeId(id))
                  .map(_ => println(s"load recipe $id successfully"))
                  .handleError(_ => println(s"recipe $id was not load"))
              }
              .flatMap(_ => Timer[IO].sleep(FiniteDuration.apply(1, TimeUnit.MINUTES)))
          }
          (0 to count).map(x => from + x * quotas).foldLeft(IO.pure()) {
            case (io, from) =>
              io.flatMap(_ => action(from))
          }
        }
      }
      .map(_ => ExitCode.Success)
  }
}
