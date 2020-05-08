package ru.pkuznetsov.app

import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import cats.implicits._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import ru.pkuznetsov.bucket.api.BucketController
import ru.pkuznetsov.bucket.dao.PostgresqlBucketDao
import ru.pkuznetsov.bucket.services.BucketService
import ru.pkuznetsov.ingredients.dao.PostgresqlIngredientNamesDao
import ru.pkuznetsov.ingredients.services.IngredientNameManager
import ru.pkuznetsov.recipes.api.RecipeController
import ru.pkuznetsov.recipes.dao.PostgresqlRecipeDao
import ru.pkuznetsov.recipes.services.{RecipeServiceImpl, RecipeTableManager}

import scala.concurrent.ExecutionContext

object AppRunner extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {

    implicit val ec = ExecutionContext.global
    val transactor: Resource[IO, HikariTransactor[IO]] =
      for {
        ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
        be <- Blocker[IO] // our blocking EC
        xa <- HikariTransactor.newHikariTransactor[IO](
          "org.postgresql.Driver",
          "jdbc:postgresql://localhost/recipes",
          "pavel",
          "password",
          ce,
          be
        )
      } yield xa

    val recipeService = {
      val recipeDao = new PostgresqlRecipeDao[IO](transactor)
      val ingredientNamesDao = new PostgresqlIngredientNamesDao[IO](transactor)
      val ingredientNameManager = new IngredientNameManager[IO](ingredientNamesDao)
      val recipeTableManager = new RecipeTableManager[IO]
      new RecipeServiceImpl[IO](recipeDao, ingredientNameManager, recipeTableManager)
    }

    val bucketService = {
      val bucketDao = new PostgresqlBucketDao[IO](transactor)
      val ingredientNamesDao = new PostgresqlIngredientNamesDao[IO](transactor)
      new BucketService[IO](bucketDao, ingredientNamesDao)
    }

    val httpApp = Router("/bucket" -> new BucketController[IO](bucketService).routes,
                         "/recipes" -> new RecipeController[IO](recipeService).routes).orNotFound

    BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }

}
