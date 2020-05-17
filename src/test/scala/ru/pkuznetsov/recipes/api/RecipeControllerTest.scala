package ru.pkuznetsov.recipes.api

import cats.effect.IO
import fs2.{Chunk, Stream}
import io.circe.syntax._
import io.circe.{Json, Printer}
import org.http4s.circe.jsonOf
import org.http4s.implicits._
import org.http4s.{Method, Request, Status}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSuite, Matchers}
import ru.pkuznetsov.ingredients.model.IngredientError.EmptyIngredientList
import ru.pkuznetsov.recipes.model.RecipeError.RecipeNotExist
import ru.pkuznetsov.recipes.services.{RecipeService, SaveResponse}
import ru.pkuznetsov.recipes.services.RecipeService.RecipeId

import scala.concurrent.ExecutionContext

class RecipeControllerTest extends FunSuite with Matchers with MockFactory {

  implicit val jsonDecoder = jsonOf[IO, Json]
  implicit val cs = IO.contextShift(ExecutionContext.global)
  val service: RecipeService[IO] = mock[RecipeService[IO]]
  val routes = new RecipeController[IO](service).routes.orNotFound

  test("get recipe by id") {
    val recipe = RecipeResponseBody(
      name = "pizza",
      uri = Some("https://tralala.com/lala/nana/haha"),
      summary = "this is pizza",
      author = "Pavel",
      cookingTime = Some(40),
      calories = Some(4),
      protein = Some(40),
      fat = Some(20),
      carbohydrates = Some(23),
      sugar = Some(10),
      ingredients = List(IngredientResponse("name1", Some(100), Some("ml")),
                         IngredientResponse("name2", Some(10), None),
                         IngredientResponse("name3", Some(0.1), Some("kg")))
    )

    service.get _ expects RecipeId(10) returns IO.pure(recipe)

    val response = routes.run(Request(method = Method.GET, uri = uri"recipe/10")).unsafeRunSync()

    response.status shouldBe Status.Ok
    response.as[Json].unsafeRunSync() shouldBe
      Json.obj(
        ("name", Json.fromString("pizza")),
        ("uri", Json.fromString("https://tralala.com/lala/nana/haha")),
        ("summary", Json.fromString("this is pizza")),
        ("author", Json.fromString("Pavel")),
        ("cookingTime", Json.fromInt(40)),
        ("calories", Json.fromDoubleOrNull(4)),
        ("protein", Json.fromDoubleOrNull(40)),
        ("fat", Json.fromDoubleOrNull(20)),
        ("carbohydrates", Json.fromDoubleOrNull(23)),
        ("sugar", Json.fromDoubleOrNull(10)),
        ("ingredients",
         Json.fromValues(
           Seq(
             Json.obj(
               ("name", Json.fromString("name1")),
               ("amount", Json.fromDoubleOrNull(100)),
               ("unit", Json.fromString("ml"))
             ),
             Json.obj(("name", Json.fromString("name2")), ("amount", Json.fromDoubleOrNull(10))),
             Json.obj(("name", Json.fromString("name3")),
                      ("amount", Json.fromDoubleOrNull(0.1)),
                      ("unit", Json.fromString("kg")))
           )))
      )
  }

  test("get recipe with id with not exist") {
    service.get _ expects RecipeId(10) returns IO.raiseError(RecipeNotExist(RecipeId(10)))

    val response = routes.run(Request(method = Method.GET, uri = uri"recipe/10")).unsafeRunSync()

    response.status shouldBe Status.BadRequest
    response.as[Json].unsafeRunSync() shouldBe Json.obj(
      ("error", Json.fromString("recipe with id 10 not exist")))
  }

  test("save recipe") {
    val recipe = RecipeRequestBody(
      name = "pizza",
      uri = Some("https://tralala.com/lala/nana/haha"),
      summary = "this is pizza",
      author = "Pavel",
      cookingTime = Some(40),
      calories = Some(4),
      protein = Some(40),
      fat = Some(20),
      carbohydrates = Some(23),
      sugar = Some(10),
      ingredients = List(IngredientRequest(1, 100, Some("ml")),
                         IngredientRequest(2, 10, None),
                         IngredientRequest(3, 0.1, Some("kg")))
    )

    service.save _ expects recipe returns IO.pure(SaveResponse(7))

    val en: Stream[IO, Byte] = Stream.chunk(Chunk.bytes(recipe.asJson.printWith(Printer.noSpaces).getBytes))
    val response = routes
      .run(Request(method = Method.POST, uri = uri"/recipe", body = en))
      .unsafeRunSync()

    response.status shouldBe Status.Ok
    response.as[Json].unsafeRunSync() shouldBe Json.obj(("id", Json.fromInt(7)))
  }

  test("save recipe without ingredients") {
    val recipe = RecipeRequestBody(
      name = "pizza",
      uri = Some("https://tralala.com/lala/nana/haha"),
      summary = "this is pizza",
      author = "Pavel",
      cookingTime = Some(40),
      calories = Some(4),
      protein = Some(40),
      fat = Some(20),
      carbohydrates = Some(23),
      sugar = Some(10),
      ingredients = List.empty
    )

    service.save _ expects recipe returns IO.raiseError(EmptyIngredientList)

    val en: Stream[IO, Byte] = Stream.chunk(Chunk.bytes(recipe.asJson.printWith(Printer.noSpaces).getBytes))
    val response = routes
      .run(Request(method = Method.POST, uri = uri"/recipe", body = en))
      .unsafeRunSync()

    response.status shouldBe Status.BadRequest
    response.as[Json].unsafeRunSync() shouldBe
      Json.obj(("error", Json.fromString("ingredient list is empty")))
  }

  test("get recipes by bucket") {
    (service.getByBucket _).expects() returns IO.pure(List(RecipeId(1), RecipeId(2)))

    val response = routes.run(Request(method = Method.GET, uri = uri"/recipe/byBucket")).unsafeRunSync()

    response.status shouldBe Status.Ok
    response.as[Json].unsafeRunSync() shouldBe Json.arr(Json.fromInt(1), Json.fromInt(2))
  }

  test("delete recipe") {
    service.delete _ expects (RecipeId(7)) returns IO.unit
    val response = routes.run(Request(method = Method.DELETE, uri = uri"/recipe/7")).unsafeRunSync()
    response.status shouldBe Status.Ok
  }

  test("delete recipe with incorrect id") {
    service.delete _ expects (RecipeId(7)) returns IO.raiseError(RecipeNotExist(RecipeId(7)))
    val response = routes.run(Request(method = Method.DELETE, uri = uri"/recipe/7")).unsafeRunSync()
    response.status shouldBe Status.BadRequest
    response.as[Json].unsafeRunSync() shouldBe
      Json.obj(("error", Json.fromString("recipe with id 7 not exist")))
  }
}
