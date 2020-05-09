package ru.pkuznetsov.bucket.api

import cats.effect.IO
import fs2.{Chunk, Stream}
import io.circe.{Json, Printer}
import org.http4s.circe.jsonOf
import org.http4s.implicits._
import org.http4s.{Method, Request, Status}
import org.scalamock.scalatest.{AsyncMockFactory, MockFactory}
import org.scalatest.{FunSuite, Matchers}
import ru.pkuznetsov.bucket.model.{Bucket, BucketEntry}
import ru.pkuznetsov.bucket.services.BucketService
import io.circe.syntax._

class BucketControllerTest extends FunSuite with Matchers with MockFactory {

  implicit val jsonDecoder = jsonOf[IO, Json]

  test("save bucket") {
    val service = mock[BucketService[IO]]

    val bucket =
      Bucket(List(BucketEntry(1, 100, Some("ml")), BucketEntry(2, 10, None), BucketEntry(3, 0.1, Some("kg"))))

    service.saveBucket _ expects bucket returns IO.unit

    val routes = new BucketController[IO](service).routes.orNotFound

    val en: Stream[IO, Byte] = Stream.chunk(Chunk.bytes(bucket.asJson.printWith(Printer.noSpaces).getBytes))
    val response = routes
      .run(Request(method = Method.POST, uri = uri"/", body = en))
      .unsafeRunSync()

    response.status shouldBe Status.Ok
    response.as[Json].unsafeRunSync() shouldBe Json.obj()
  }
}
