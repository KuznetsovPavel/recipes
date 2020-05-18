package ru.pkuznetsov.loaders.services

import java.net.URI

import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFunSuite, Matchers}
import ru.pkuznetsov.core.utils.FutureSyncForTest
import ru.pkuznetsov.ingredients.model.IngredientName
import ru.pkuznetsov.ingredients.services.IngredientNameManager
import ru.pkuznetsov.loaders.connectors.spoonacular.RecipeLoader
import ru.pkuznetsov.loaders.connectors.spoonacular.SpoonacularLoader.LoaderRecipeId
import ru.pkuznetsov.loaders.model.LoaderError.DuplicateIngredients
import ru.pkuznetsov.recipes.dao.{IngredientRow, RecipeDao, RecipeRow}
import ru.pkuznetsov.recipes.model.{Ingredient, Recipe}
import ru.pkuznetsov.recipes.services.RecipeService.{IngredientId, RecipeId}
import ru.pkuznetsov.recipes.services.RecipeTableManagerImpl

import scala.concurrent.Future

class LoaderServiceTest extends AsyncFunSuite with Matchers with AsyncMockFactory {

  implicit val futureSync = new FutureSyncForTest().futureSync
  val recipeLoader = mock[RecipeLoader[Future]]
  val recipeDao = mock[RecipeDao[Future]]
  val ingNameManager = mock[IngredientNameManager[Future]]
  val recipeTableManager = new RecipeTableManagerImpl[Future]()

  val service = new LoaderService[Future](recipeLoader, recipeDao, ingNameManager, recipeTableManager)

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

  val recipeRow = RecipeRow(
    recipeId = 42,
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

  val ingredientsRow = List(IngredientRow(0, 3, 0.1, Some("kg")),
                            IngredientRow(0, 2, 10, None),
                            IngredientRow(0, 1, 100, Some("ml")))

  test("loader return correct recipe") {
    recipeLoader.getRecipe _ expects LoaderRecipeId(700) returns Future.successful(recipe)
    ingNameManager.addAndGetNames _ expects List("name3", "name2", "name1") returns Future.successful(
      List(IngredientName(IngredientId(1), "name1"),
           IngredientName(IngredientId(2), "name2"),
           IngredientName(IngredientId(3), "name3")))
    recipeDao.saveRecipeWithIngredients _ expects (recipeRow, ingredientsRow) returns Future.successful(
      RecipeId(7))

    service.loadAndSave(LoaderRecipeId(700)).map(_ shouldBe RecipeId(7))
  }

  test("loader recipe with incorrect ingredients") {
    val incorrectRecipe =
      recipe.copy(ingredients = recipe.ingredients :+ recipe.ingredients.head.copy(unit = None))
    recipeLoader.getRecipe _ expects LoaderRecipeId(700) returns Future.successful(incorrectRecipe)
    recoverToSucceededIf[DuplicateIngredients](service.loadAndSave(LoaderRecipeId(700)))
  }

}
