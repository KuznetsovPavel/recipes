package ru.pkuznetsov.app

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.semigroupk._
import com.softwaremill.macwire.wire
import com.typesafe.config.ConfigFactory
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import ru.pkuznetsov.bucket.api.BucketController
import ru.pkuznetsov.bucket.dao.PostgresqlBucketDao
import ru.pkuznetsov.bucket.services.BucketServiceImpl
import ru.pkuznetsov.ingredients.dao.PostgresqlIngredientNamesDao
import ru.pkuznetsov.ingredients.services.IngredientNameManagerImpl
import ru.pkuznetsov.recipes.api.RecipeController
import ru.pkuznetsov.recipes.dao.PostgresqlRecipeDao
import ru.pkuznetsov.recipes.services.{RecipeServiceImpl, RecipeTableManagerImpl}
import sttp.tapir.Endpoint
import sttp.tapir.docs.openapi._
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.swagger.http4s.SwaggerHttp4s

object AppRunner extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val config = ConfigFactory.load()
    val transactor = PsqlIOTranscator.create(config.getString("psql-user"), config.getString("psql-password"))
    val recipeDao = wire[PostgresqlRecipeDao[IO]]
    val ingredientNamesDao = wire[PostgresqlIngredientNamesDao[IO]]
    val ingredientNameManager = wire[IngredientNameManagerImpl[IO]]
    val recipeTableManager = wire[RecipeTableManagerImpl[IO]]
    val bucketDao = wire[PostgresqlBucketDao[IO]]
    val recipeService = wire[RecipeServiceImpl[IO]]
    val bucketService = wire[BucketServiceImpl[IO]]

    val recipe = wire[RecipeController[IO]]
    val bucket = wire[BucketController[IO]]

    val endpoints: Seq[Endpoint[_, _, _, _]] = recipe.endpoints ++ bucket.endpoints
    val openApiDocs = endpoints.toOpenAPI("Recipe service", "1.0.0")
    val openApiYml = openApiDocs.toYaml

    val routes: HttpRoutes[IO] = recipe.routes <+> bucket.routes <+> new SwaggerHttp4s(openApiYml).routes[IO]
    val httpApp = Router("/" -> routes).orNotFound

    BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }

}
