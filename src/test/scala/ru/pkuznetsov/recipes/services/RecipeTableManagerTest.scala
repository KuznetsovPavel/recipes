//package ru.pkuznetsov.recipes.services
//
//import java.net.URI
//
//import cats.instances.future._
//import org.scalatest.{AsyncFunSuite, Matchers}
//import ru.pkuznetsov.core.model.AppError.{CannotFindIngredients, CannotParseURI, RecipeNotExist}
//import ru.pkuznetsov.recipes.dao.{IngredientRow, RecipeRow}
//import ru.pkuznetsov.recipes.model.RecipeError.CannotParseURI
//import ru.pkuznetsov.recipes.model.{Ingredient, Recipe}
//import ru.pkuznetsov.recipes.services.RecipeService.RecipeId
//
//import scala.concurrent.Future
//
//class RecipeTableManagerTest extends AsyncFunSuite with Matchers {
//
//  val manager = new RecipeTableManager[Future]()
//
//  val recipeRow = RecipeRow(
//    recipeId = 0,
//    name = "pizza",
//    uri = Some("https://tralala.com/lala/nana/haha"),
//    summary = "this is pizza",
//    author = "Pavel",
//    cookingTime = Some(40),
//    calories = Some(4),
//    protein = Some(40),
//    fat = Some(20),
//    carbohydrates = Some(23),
//    sugar = Some(10)
//  )
//
//  val ingredients = List(IngredientRow(0, 1, 100, Some("ml")),
//                         IngredientRow(0, 2, 10, None),
//                         IngredientRow(0, 3, 0.1, Some("kg")))
//
//  val names = Map(1 -> "name1", 2 -> "name2", 3 -> "name3")
//
//  val recipe = Recipe(
//    id = 0,
//    name = "pizza",
//    uri = Some(URI.create("https://tralala.com/lala/nana/haha")),
//    summary = "this is pizza",
//    author = "Pavel",
//    cookingTime = Some(40),
//    calories = Some(4),
//    protein = Some(40),
//    fat = Some(20),
//    carbohydrates = Some(23),
//    sugar = Some(10),
//    ingredients = List(Ingredient(1, "name1", 100, Some("ml")),
//                       Ingredient(2, "name2", 10, None),
//                       Ingredient(3, "name3", 0.1, Some("kg")))
//  )
//
//  test("correct data") {
//    manager.createRecipeFrom(RecipeId(0), Some(recipeRow), ingredients, names).map { result =>
//      result shouldBe recipe
//    }
//  }
//
//  test("no recipe") {
//    recoverToSucceededIf[RecipeNotExist](manager.createRecipeFrom(RecipeId(0), None, ingredients, names))
//  }
//
//  test("no ingredient name") {
//    recoverToSucceededIf[CannotFindIngredients](
//      manager.createRecipeFrom(RecipeId(0), Some(recipeRow), ingredients, names.removed(1)))
//  }
//
//  test("incorrect uri") {
//    recoverToSucceededIf[CannotParseURI](
//      manager.createRecipeFrom(RecipeId(0), Some(recipeRow.copy(uri = Some("!@$%"))), ingredients, names))
//  }
//
//}
