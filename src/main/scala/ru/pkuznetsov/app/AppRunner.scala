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
import ru.pkuznetsov.recipes.api.RecipeController
import ru.pkuznetsov.recipes.dao.{PostgresqlRecipeDao, RecipeTableManager}
import ru.pkuznetsov.recipes.services.RecipeServiceImpl

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
      val daoManager = new RecipeTableManager[IO]()
      val dao = new PostgresqlRecipeDao[IO](transactor, daoManager)
      new RecipeServiceImpl[IO](dao)
    }

    val bucketService = {
      val dao = new PostgresqlBucketDao[IO](transactor)
      new BucketService[IO](dao)
    }

    val httpApp = Router("/bucket" -> new BucketController[IO](bucketService).routes,
                         "/recipes" -> new RecipeController[IO](recipeService).routes).orNotFound

    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }

}
