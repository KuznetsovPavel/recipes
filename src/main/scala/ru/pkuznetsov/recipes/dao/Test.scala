package ru.pkuznetsov.recipes.dao

import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import ru.pkuznetsov.recipes.model.{Ingredient, Recipe}

import scala.concurrent.ExecutionContext

object Test extends IOApp {
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

    val recipe = Recipe(
      0,
      "pizza",
      None,
      "it is perfect",
      "Pavel",
      Some(40),
      Some(100),
      None,
      None,
      Some(33.3),
      Some(12.34),
      List(Ingredient(0, "name1", 12.34, None), Ingredient(0, "name2", 222.34, Some("uunit")))
    )

    val dao = new PostgresqlRecipeDao[IO](transactor)
    for {
      _ <- dao.createTables
      ids <- dao.insertRecipe(recipe)
      _ <- IO(println(ids))
    } yield ExitCode.Success
  }
}
