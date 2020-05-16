package ru.pkuznetsov.recipes.services

import java.net.URI

import cats.data.NonEmptyList
import cats.instances.future._
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFunSuite, Matchers}
import ru.pkuznetsov.bucket.dao.BucketDao
import ru.pkuznetsov.bucket.model.{Bucket, BucketEntry}
import ru.pkuznetsov.ingredients.model.IngredientName
import ru.pkuznetsov.ingredients.services.IngredientNameManager
import ru.pkuznetsov.recipes.api.{
  IngredientRequest,
  IngredientResponse,
  RecipeRequestBody,
  RecipeResponseBody
}
import ru.pkuznetsov.recipes.dao.{IngredientRow, RecipeDao, RecipeRow}
import ru.pkuznetsov.recipes.model.RecipeError.{BucketNotExist, CannotParseURI}
import ru.pkuznetsov.recipes.model.{Ingredient, Recipe}
import ru.pkuznetsov.recipes.services.RecipeService.{IngredientId, RecipeId}

import scala.concurrent.{ExecutionContext, Future}

class RecipeServiceTest extends AsyncFunSuite with Matchers with AsyncMockFactory {

  implicit val ex = ExecutionContext.global

  val recipe = Recipe(
    id = 42,
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

  val recipeResponse = RecipeResponseBody(
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

  val recipeRequest = RecipeRequestBody(
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

  val recipeRow = RecipeRow(
    recipeId = 0,
    name = "pizza",
    uri = Some("https://tralala.com/lala/nana/haha"),
    summary = "this is pizza",
    author = "Pavel",
    cookingTime = Some(40),
    calories = Some(4),
    protein = Some(40),
    fat = Some(20),
    carbohydrates = Some(23),
    sugar = Some(10)
  )

  val ingredientsRow = List(IngredientRow(0, 1, 100, Some("ml")),
                            IngredientRow(0, 2, 10, None),
                            IngredientRow(0, 3, 0.1, Some("kg")))

  val recipeDao = mock[RecipeDao[Future]]
  val nameManager = mock[IngredientNameManager[Future]]
  val tableManager = mock[RecipeTableManager[Future]]
  val bucketDao = mock[BucketDao[Future]]
  val service = new RecipeServiceImpl[Future](recipeDao, bucketDao, nameManager, tableManager)

  test("save recipe") {
    nameManager.checkIngredientIds _ expects * returns Future.unit
    recipeDao.saveRecipeWithIngredients _ expects (recipeRow, ingredientsRow) returns Future.successful(
      RecipeId(7))
    tableManager.recipeRequest2RecipeRow _ expects recipeRequest returns recipeRow
    recipeRequest.ingredients.zip(ingredientsRow).foreach {
      case (ingReq, ingRow) =>
        tableManager.ingRequest2IngRow _ expects (ingReq, RecipeId(0)) returns ingRow
    }

    service.save(recipeRequest).map(result => result shouldBe SaveResponse(7))
  }

  test("save recipe with incorrect uri") {
    recoverToSucceededIf[CannotParseURI](service.save(recipeRequest.copy(uri = Some("!@#$%%^"))))
  }

  test("get recipe") {
    val names = List(IngredientName(IngredientId(1), "name1"),
                     IngredientName(IngredientId(2), "name2"),
                     IngredientName(IngredientId(3), "name3"))
    recipeDao.getRecipe _ expects RecipeId(42) returns Future.successful(Some(recipeRow))
    recipeDao.getIngredientForRecipe _ expects RecipeId(42) returns Future.successful(ingredientsRow)
    nameManager.getIngredientNamesFor _ expects List(1, 2, 3).map(x => IngredientId(x)) returns Future
      .successful(names)
    tableManager.createRecipeFrom _ expects (recipeRow, ingredientsRow, names) returns
      Future.successful(recipe)

    service.get(RecipeId(42)).map(result => result shouldBe recipeResponse)
  }

  test("get recipe by bucket") {
    val bucket = Bucket(
      List(
        BucketEntry(1, 1.2, Some("ml")),
        BucketEntry(2, 2.2, None),
        BucketEntry(3, 4.2, Some("ml"))
      ))

    (bucketDao.getBucket _).expects() returns Future.successful(Some(bucket))

    recipeDao.getRecipesByIngredients _ expects NonEmptyList.of(IngredientId(1),
                                                                IngredientId(2),
                                                                IngredientId(3)) returns
      Future.successful(List(RecipeId(1), RecipeId(2)))

    service.getByBucket.map(result => result shouldBe List(RecipeId(1), RecipeId(2)))
  }

  test("get recipe without bucket") {
    (bucketDao.getBucket _).expects() returns Future.successful(None)
    recoverToSucceededIf[BucketNotExist.type](service.getByBucket)
  }

}
