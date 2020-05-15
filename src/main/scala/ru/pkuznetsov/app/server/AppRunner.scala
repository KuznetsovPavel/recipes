package ru.pkuznetsov.app.server

import cats.effect.{ExitCode, IO, IOApp}
import ru.pkuznetsov.app.server.components.{
  ConfigComponent,
  ControllerComponents,
  RoutesComponent,
  ServerComponent
}

object AppRunner extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val resources = for {
      config <- ConfigComponent[IO]()
      controllers <- ControllerComponents[IO](config)
      route <- RoutesComponent[IO](controllers)
      server <- ServerComponent[IO](config, route)
    } yield server

    resources.use(_ => IO.never)

  }

}
