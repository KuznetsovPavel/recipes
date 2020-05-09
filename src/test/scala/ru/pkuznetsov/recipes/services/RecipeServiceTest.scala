package ru.pkuznetsov.recipes.services

import java.net.URI

import cats.instances.future._
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFunSuite, Matchers}
import ru.pkuznetsov.ingredients.model.IngredientName
import ru.pkuznetsov.ingredients.services.IngredientNameManager
import ru.pkuznetsov.recipes.api.{IngredientRequest, RecipeRequestBody}
import ru.pkuznetsov.recipes.dao.{IngredientRow, RecipeDao, RecipeRow}
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

  val recipeRequest = RecipeRequestBody(
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
  val service = new RecipeServiceImpl[Future](recipeDao, nameManager, tableManager)

  test("save recipe") {
    nameManager.checkIngredientIds _ expects * returns Future.unit
    recipeDao.saveRecipeWithIngredients _ expects (recipeRow, ingredientsRow) returns Future.successful(7)
    tableManager.recipeRequest2RecipeRow _ expects recipeRequest returns recipeRow
    recipeRequest.ingredients.zip(ingredientsRow).foreach {
      case (ingReq, ingRow) =>
        tableManager.ingRequest2IngRow _ expects (ingReq, RecipeId(0)) returns ingRow
    }

    service.save(recipeRequest).map(result => result shouldBe RecipeId(7))
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

    service.get(RecipeId(42)).map(result => result shouldBe recipe)
  }

}
