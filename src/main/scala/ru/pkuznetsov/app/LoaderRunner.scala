package ru.pkuznetsov.app

import cats.effect.{ExitCode, IO, IOApp}
import cats.instances.list._
import cats.syntax.traverse._
import com.typesafe.config.ConfigFactory
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

    val config = ConfigFactory.load()
    implicit val ec = ExecutionContext.global

    def getService(backend: Backend[IO]): IO[LoaderService[IO]] = {
      import com.softwaremill.macwire._
      val apiKey = SpoonacularApiKey(config.getString("spoonacular-api-key"))
      val transactor =
        PsqlIOTranscator.create(config.getString("psql-user"), config.getString("psql-password"))
      val loader = new SpoonacularLoader(backend, apiKey)
      val recipeDao = wire[PostgresqlRecipeDao[IO]]
      val ingNameDao = wire[PostgresqlIngredientNamesDao[IO]]
      val nameManager = wire[IngredientNameManagerImpl[IO]]
      val recipeTableManager = wire[RecipeTableManagerImpl[IO]]
      IO.pure(wire[LoaderService[IO]])
    }

    for {
      backend <- AsyncHttpClientCatsBackend[IO]()
      service <- getService(backend)
      res <- (326 to 340).toList.traverse { x =>
        service.loadAndSave(LoaderRecipeId(x)).handleErrorWith(_ => IO.pure(-x))
      }
      _ = res.foreach(println)
      _ <- backend.close()
    } yield ExitCode.Success

  }
}
