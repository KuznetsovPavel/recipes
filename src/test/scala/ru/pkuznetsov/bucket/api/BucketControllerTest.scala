package ru.pkuznetsov.bucket.api

import cats.effect.IO
import fs2.{Chunk, Stream}
import io.circe.syntax._
import io.circe.{Json, Printer}
import org.http4s.circe.jsonOf
import org.http4s.implicits._
import org.http4s.{Method, Request, Status}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSuite, Matchers}
import ru.pkuznetsov.bucket.model.{Bucket, BucketEntry}
import ru.pkuznetsov.bucket.services.BucketService

class BucketControllerTest extends FunSuite with Matchers with MockFactory {

  implicit val jsonDecoder = jsonOf[IO, Json]

  val service = mock[BucketService[IO]]

  val bucket =
    Bucket(List(BucketEntry(1, 100, Some("ml")), BucketEntry(2, 10, None), BucketEntry(3, 0.1, Some("kg"))))

  val routes = new BucketController[IO](service).routes.orNotFound

  test("save bucket") {
    service.saveBucket _ expects bucket returns IO.unit

    val en: Stream[IO, Byte] = Stream.chunk(Chunk.bytes(bucket.asJson.printWith(Printer.noSpaces).getBytes))
    val response = routes
      .run(Request(method = Method.POST, uri = uri"/", body = en))
      .unsafeRunSync()

    response.status shouldBe Status.Ok
    response.as[Json].unsafeRunSync() shouldBe Json.obj()
  }

  test("get bucket") {
    (service.getBucket _).expects() returns IO.pure(bucket)

    val response = routes
      .run(Request(method = Method.GET, uri = uri"/"))
      .unsafeRunSync()

    response.status shouldBe Status.Ok
    response.as[Json].unsafeRunSync() shouldBe Json.obj(
      ("ingredients",
       Json.arr(
         Json.obj(("ingredientId", Json.fromInt(1)),
                  ("amount", Json.fromDoubleOrNull(100)),
                  ("unit", Json.fromString("ml"))),
         Json.obj(("ingredientId", Json.fromInt(2)), ("amount", Json.fromDoubleOrNull(10))),
         Json.obj(("ingredientId", Json.fromInt(3)),
                  ("amount", Json.fromDoubleOrNull(0.1)),
                  ("unit", Json.fromString("kg")))
       )))
  }
}
