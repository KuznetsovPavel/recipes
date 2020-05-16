package ru.pkuznetsov.ingredients.api

import cats.effect.IO
import io.circe.Json
import org.http4s.implicits._
import org.http4s.{Method, Request, Status}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSuite, Matchers}
import ru.pkuznetsov.ingredients.model.{IngredientName, IngredientNameService}

import scala.concurrent.ExecutionContext
import org.http4s.circe.jsonOf

class IngredientNameControllerTest extends FunSuite with Matchers with MockFactory {

  implicit val jsonDecoder = jsonOf[IO, Json]
  implicit val cs = IO.contextShift(ExecutionContext.global)

  val service = mock[IngredientNameService[IO]]

  val routes = new IngredientNamesController[IO](service).routes.orNotFound

  test("get all ingredient names") {
    val ingNames = List(IngredientName(1, "name1"), IngredientName(2, "name2"), IngredientName(3, "name3"))

    (service.getIngredientNames _).expects() returns IO.pure(ingNames)

    val response = routes
      .run(Request(method = Method.GET, uri = uri"/ingredients"))
      .unsafeRunSync()

    response.status shouldBe Status.Ok
    response.as[Json].unsafeRunSync() shouldBe Json.arr(
      Json.obj(("id", Json.fromInt(1)), ("name", Json.fromString("name1"))),
      Json.obj(("id", Json.fromInt(2)), ("name", Json.fromString("name2"))),
      Json.obj(("id", Json.fromInt(3)), ("name", Json.fromString("name3")))
    )
  }

}
