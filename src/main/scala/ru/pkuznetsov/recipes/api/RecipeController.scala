package ru.pkuznetsov.recipes.api

import cats.implicits._
import cats.{Defer, Monad, MonadError}
import io.circe.parser.decode
import org.http4s.HttpRoutes
import ru.pkuznetsov.core.Http4sController
import ru.pkuznetsov.recipes.model.Recipe
import ru.pkuznetsov.recipes.services.RecipeService

class RecipeController[F[_]: Defer: Monad](service: RecipeService[F])(
    implicit monad: MonadError[F, Throwable])
    extends Http4sController[F] {

  override val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case recipe @ POST -> Root / "recipes" =>
      for {
        recipe <- monad.fromEither(decode[Recipe](recipe.queryString))
        response <- service.save(recipe)
      } yield Ok(response.toString)

  }

}

//object Test extends IOApp {
//  override def run(args: List[String]): IO[ExitCode] = {
//    import org.http4s.implicits._
//    import cats.implicits._
//    val httpApp = Router("/" -> new RecipeController[IO].routes).orNotFound
//
//    BlazeServerBuilder[IO]
//      .bindHttp(8080, "localhost")
//      .withHttpApp(httpApp)
//      .serve
//      .compile
//      .drain
//      .as(ExitCode.Success)
//  }
//}
