package ru.pkuznetsov.app.utill

import cats.effect.{Async, Blocker, ContextShift, Resource, Sync}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object PsqlIOTranscator {

  def create[F[_]: Sync: Async](user: String, password: String, dbUrl: String, threadNum: Int)(
      implicit cs: ContextShift[F]): Resource[F, HikariTransactor[F]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[F](threadNum)
      be <- Blocker[F]
      xa <- HikariTransactor.newHikariTransactor[F](
        "org.postgresql.Driver",
        dbUrl,
        user,
        password,
        ce,
        be
      )
    } yield xa

}
