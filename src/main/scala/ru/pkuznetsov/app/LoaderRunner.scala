package ru.pkuznetsov.app

import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import cats.instances.list._
import cats.syntax.traverse._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import ru.pkuznetsov.ingredients.dao.PostgresqlIngredientNamesDao
import ru.pkuznetsov.ingredients.services.IngredientNameManagerImpl
import ru.pkuznetsov.loaders.connectors.spoonacular.SpoonacularLoader
import ru.pkuznetsov.loaders.connectors.spoonacular.SpoonacularLoader.{
  Backend,
  LoaderRecipeId,
  SpoonacularApiKey
}
import ru.pkuznetsov.loaders.services.LoaderService
import ru.pkuznetsov.recipes.dao.PostgresqlRecipeDao
import ru.pkuznetsov.recipes.services.RecipeTableManagerImpl
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend

import scala.concurrent.ExecutionContext

object LoaderRunner extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {

    implicit val ec = ExecutionContext.global
    val transactor: Resource[IO, HikariTransactor[IO]] =
      for {
        ce <- ExecutionContexts.fixedThreadPool[IO](2) // our connect EC
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

    val apiKey = SpoonacularApiKey("9be944945f3548d4854ea918c6e13963")

    def getService(backend: Backend[IO]): IO[LoaderService[IO]] = {
      val loader = new SpoonacularLoader(backend, apiKey)
      val recipeDao = new PostgresqlRecipeDao[IO](transactor)
      val ingNameDao = new PostgresqlIngredientNamesDao[IO](transactor)
      val nameManager = new IngredientNameManagerImpl[IO](ingNameDao)
      val recipeTableManager = new RecipeTableManagerImpl[IO]()
      IO.pure(new LoaderService[IO](loader, recipeDao, nameManager, recipeTableManager))
    }

    for {
      backend <- AsyncHttpClientCatsBackend[IO]()
      service <- getService(backend)
      res <- (1 to 10).toList.traverse { x =>
        service.loadAndSave(LoaderRecipeId(x)).handleErrorWith(_ => IO.pure(-x))
      }
      _ = res.foreach(println)
      _ <- backend.close()
    } yield ExitCode.Success

  }
}
