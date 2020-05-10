package ru.pkuznetsov.recipes.api

import java.net.URI

import cats.effect.IO
import org.http4s.implicits._
import org.http4s.{Method, Request, Status}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSuite, Matchers}
import ru.pkuznetsov.recipes.model.{Ingredient, Recipe}
import ru.pkuznetsov.recipes.services.RecipeService
import ru.pkuznetsov.recipes.services.RecipeService.RecipeId

class RecipeControllerTest extends FunSuite with Matchers with MockFactory {

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

  test("get recipe by id") {
    val service: RecipeService[IO] = mock[RecipeService[IO]]
    service.get _ expects RecipeId(10) returns IO.pure(recipe)

    val routes = new RecipeController[IO](service).routes.orNotFound

    val response = routes.run(Request(method = Method.GET, uri = uri"/recipes/10")).unsafeRunSync()

    response.status shouldBe Status.Ok
    response.as[Recipe].unsafeRunSync() shouldBe recipe
  }

//  test("save recipe") {
//    val service: RecipeService[IO] = mock[RecipeService[IO]]
//    service.save _ expects recipe returns IO.pure(7)
//
//    val routes = new RecipeController[IO](service).routes.orNotFound
//
//    val response = routes.run(Request(method = Method.POST, uri = uri"/recipes")).unsafeRunSync()
//
//    response.status shouldBe Status.Ok
//    response.as[Int].unsafeRunSync() shouldBe 7
//  }
}
