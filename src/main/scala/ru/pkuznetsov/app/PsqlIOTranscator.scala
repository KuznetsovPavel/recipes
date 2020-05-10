package ru.pkuznetsov.app

import cats.effect.{Blocker, ContextShift, IO, Resource}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object PsqlIOTranscator {

  def create(user: String, password: String)(
      implicit cs: ContextShift[IO]): Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32)
      be <- Blocker[IO]
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.postgresql.Driver",
        "jdbc:postgresql://localhost/recipes",
        user,
        password,
        ce,
        be
      )
    } yield xa

}
