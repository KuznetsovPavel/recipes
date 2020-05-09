package ru.pkuznetsov.recipes.api

import java.net.URI

import cats.effect.IO
import fs2.{Chunk, Stream}
import io.circe.syntax._
import io.circe.{Json, Printer}
import org.http4s.circe.jsonOf
import org.http4s.implicits._
import org.http4s.{EntityDecoder, Method, Request, Status}
import org.scalamock.scalatest.{AsyncMockFactory, MockFactory}
import org.scalatest.{FunSuite, Matchers}
import ru.pkuznetsov.ingredients.model.IngredientError.EmptyIngredientList
import ru.pkuznetsov.recipes.model.RecipeError.RecipeNotExist
import ru.pkuznetsov.recipes.model.{Ingredient, Recipe}
import ru.pkuznetsov.recipes.services.RecipeService
import ru.pkuznetsov.recipes.services.RecipeService.RecipeId

class RecipeControllerTest extends FunSuite with Matchers with MockFactory {

  implicit val jsonDecoder = jsonOf[IO, Json]

  test("get recipe by id") {
    val service: RecipeService[IO] = mock[RecipeService[IO]]

    val recipe = Recipe(
      id = 0,
      name = "pizza",
      uri = Some(URI.create("https://tralala.com/lala/nana/haha")),
      summary = "this is pizza",
      author = "Pavel",
      cookingTime = Some(40),
      calories = Some(4),
      protein = Some(40),
      fat = Some(20),
      carbohydrates = Some(23),
      sugar = Some(10),
      ingredients = List(Ingredient(1, "name1", 100, Some("ml")),
                         Ingredient(2, "name2", 10, None),
                         Ingredient(3, "name3", 0.1, Some("kg")))
    )

    service.get _ expects RecipeId(10) returns IO.pure(recipe)

    val routes = new RecipeController[IO](service).routes.orNotFound

    val response = routes.run(Request(method = Method.GET, uri = uri"/10")).unsafeRunSync()

    response.status shouldBe Status.Ok
    response.as[Json].unsafeRunSync() shouldBe
      Json.obj(
        ("id", Json.fromInt(0)),
        ("name", Json.fromString("pizza")),
        ("uri", Json.fromString("https://tralala.com/lala/nana/haha")),
        ("summary", Json.fromString("this is pizza")),
        ("author", Json.fromString("Pavel")),
        ("cookingTime", Json.fromDoubleOrNull(40)),
        ("calories", Json.fromDoubleOrNull(4)),
        ("protein", Json.fromDoubleOrNull(40)),
        ("fat", Json.fromDoubleOrNull(20)),
        ("carbohydrates", Json.fromDoubleOrNull(23)),
        ("sugar", Json.fromDoubleOrNull(10)),
        ("ingredients",
         Json.fromValues(
           Seq(
             Json.obj(
               ("id", Json.fromInt(1)),
               ("name", Json.fromString("name1")),
               ("amount", Json.fromDoubleOrNull(100)),
               ("unit", Json.fromString("ml"))
             ),
             Json.obj(("id", Json.fromInt(2)),
                      ("name", Json.fromString("name2")),
                      ("amount", Json.fromDoubleOrNull(10))),
             Json.obj(("id", Json.fromInt(3)),
                      ("name", Json.fromString("name3")),
                      ("amount", Json.fromDoubleOrNull(0.1)),
                      ("unit", Json.fromString("kg")))
           )))
      )
  }

  test("get recipe with id with not exist") {
    val service: RecipeService[IO] = mock[RecipeService[IO]]
    service.get _ expects RecipeId(10) returns IO.raiseError(RecipeNotExist(RecipeId(10)))

    val routes = new RecipeController[IO](service).routes.orNotFound
    val response = routes.run(Request(method = Method.GET, uri = uri"/10")).unsafeRunSync()

    response.status shouldBe Status.Ok
    response.as[Json].unsafeRunSync() shouldBe Json.obj(
      ("error", Json.fromString("recipe with id 10 not exist")))
  }

  test("get recipe by incorrect id") {
    val service: RecipeService[IO] = mock[RecipeService[IO]]

    val routes = new RecipeController[IO](service).routes.orNotFound
    val response = routes.run(Request(method = Method.GET, uri = uri"/ten")).unsafeRunSync()

    response.status shouldBe Status.Ok
    response.as[Json].unsafeRunSync() shouldBe
      Json.obj(("error", Json.fromString("id should be number")))
  }

  test("save recipe") {
    val service: RecipeService[IO] = mock[RecipeService[IO]]

    val recipe = RecipeRequestBody(
      name = "pizza",
      uri = Some(URI.create("https://tralala.com/lala/nana/haha")),
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

    service.save _ expects recipe returns IO.pure(RecipeId(7))

    val routes = new RecipeController[IO](service).routes.orNotFound

    val en: Stream[IO, Byte] = Stream.chunk(Chunk.bytes(recipe.asJson.printWith(Printer.noSpaces).getBytes))
    val response = routes
      .run(Request(method = Method.POST, uri = uri"/", body = en))
      .unsafeRunSync()

    implicit val recipeDecoder: EntityDecoder[IO, Int] = jsonOf[IO, Int]

    response.status shouldBe Status.Ok
    response.as[Json].unsafeRunSync() shouldBe Json.obj(("id", Json.fromInt(7)))
  }

  test("save recipe without ingredients") {
    val service: RecipeService[IO] = mock[RecipeService[IO]]

    val recipe = RecipeRequestBody(
      name = "pizza",
      uri = Some(URI.create("https://tralala.com/lala/nana/haha")),
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

    val routes = new RecipeController[IO](service).routes.orNotFound

    val en: Stream[IO, Byte] = Stream.chunk(Chunk.bytes(recipe.asJson.printWith(Printer.noSpaces).getBytes))
    val response = routes
      .run(Request(method = Method.POST, uri = uri"/", body = en))
      .unsafeRunSync()

    implicit val recipeDecoder: EntityDecoder[IO, Int] = jsonOf[IO, Int]

    response.status shouldBe Status.Ok
    response.as[Json].unsafeRunSync() shouldBe
      Json.obj(("error", Json.fromString("ingredient list is empty")))
  }
}
