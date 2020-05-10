package ru.pkuznetsov.app

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.softwaremill.macwire._
import com.typesafe.config.ConfigFactory
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

import scala.concurrent.ExecutionContext

object AppRunner extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    implicit val ec = ExecutionContext.global
    val config = ConfigFactory.load()
    val transactor = PsqlIOTranscator.create(config.getString("psql-user"), config.getString("psql-password"))
    val recipeDao = wire[PostgresqlRecipeDao[IO]]
    val ingredientNamesDao = wire[PostgresqlIngredientNamesDao[IO]]
    val ingredientNameManager = wire[IngredientNameManagerImpl[IO]]
    val recipeTableManager = wire[RecipeTableManagerImpl[IO]]
    val bucketDao = wire[PostgresqlBucketDao[IO]]
    val recipeService = wire[RecipeServiceImpl[IO]]
    val bucketService = wire[BucketServiceImpl[IO]]

    val httpApp =
      Router("/bucket" -> wire[BucketController[IO]].routes, "/recipes" -> wire[RecipeController[IO]].routes).orNotFound

    BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }

}
