package ru.pkuznetsov.core.dao

import cats.effect.IO
import doobie.util.transactor.Transactor
import org.scalatest.FunSuite

import scala.concurrent.ExecutionContext

abstract class DbTest extends FunSuite with doobie.scalatest.IOChecker {

  implicit val cs = IO.contextShift(ExecutionContext.global)

  override def transactor = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:recipes",
    "pavel",
    "password"
  )
}
