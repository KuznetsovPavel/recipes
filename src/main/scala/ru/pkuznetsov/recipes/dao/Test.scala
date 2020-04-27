package ru.pkuznetsov.recipes.dao

import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import cats.implicits._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import ru.pkuznetsov.recipes.model.Ingredient

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

    val ingredients = List(Ingredient(0, "solt", 1, Some("measure")),
      Ingredient(0, "solt1", 1, None),
      Ingredient(0, "solt2", 14.3, Some("measure")),
      Ingredient(0, "solt4", 15.3, Some("measure3"))
    )
    val dao = new PostgresqlRecipeDao[IO](transactor)
    for {
      _ <- dao.createTables
      ids <- ingredients.traverse(dao.insertIngredient)
      _ <- IO(println(ids))
    } yield ExitCode.Success
  }
}
