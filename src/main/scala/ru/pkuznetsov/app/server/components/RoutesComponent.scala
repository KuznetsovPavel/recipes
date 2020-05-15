package ru.pkuznetsov.app.server.components

import cats.effect.{ContextShift, Resource, Sync}
import cats.syntax.semigroupk._
import org.http4s.HttpRoutes
import ru.pkuznetsov.core.api.Http4sController
import sttp.tapir.Endpoint
import sttp.tapir.docs.openapi._
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.swagger.http4s.SwaggerHttp4s

object RoutesComponent {

  def apply[F[_]: Sync](controllers: List[Http4sController[F]])(
      implicit cs: ContextShift[F]): Resource[F, HttpRoutes[F]] =
    Resource.liftF {
      Sync[F].delay {
        val endpoints: Seq[Endpoint[_, _, _, _]] = controllers.map(_.endpoints).reduce(_ ++ _)
        val openApiDocs = endpoints.toOpenAPI("Recipe service", "1.0.0")
        val openApiYml = openApiDocs.toYaml
        controllers.map(_.routes).fold(new SwaggerHttp4s(openApiYml).routes[F])(_ <+> _)
      }
    }

}
