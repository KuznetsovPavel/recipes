package ru.pkuznetsov.loaders.connectors.spoonacular

import java.net.URI

import cats.instances.future._
import io.circe.Json
import io.circe.parser.parse
import org.scalatest.{AsyncFunSuite, Matchers}
import ru.pkuznetsov.loaders.model.LoaderError
import ru.pkuznetsov.recipes.model.{Ingredient, Recipe}

import scala.concurrent.Future
import scala.io.Source

class SpoonacularDeserializatorTest extends AsyncFunSuite with Matchers {

  test("parse correct spoonacular data") {
    val recipe = Recipe(
      id = 0,
      name = "Marinated Boquerones",
      uri = Some(URI.create("http://www.deliciousdays.com/archives/2006/07/23/boqueroneshave-no-fear/")),
      summary =
        "You can never have too many main course recipes, so give Marinated Boquerones a try. This recipe serves 6. Watching your figure? This caveman, gluten free, dairy free, and primal recipe has <b>243 calories</b>, <b>17g of protein</b>, and <b>18g of fat</b> per serving. For <b>$3.76 per serving</b>, this recipe <b>covers 16%</b> of your daily requirements of vitamins and minerals. 6 people have tried and liked this recipe. A mixture of parsley, chillies, coarse sea salt, and a handful of other ingredients are all it takes to make this recipe so tasty. To use up the extra-virgin olive oil you could follow this main course with the <a href=\"https://spoonacular.com/recipes/chocolate-date-caramel-walnut-tart-gluten-free-grain-free-vegan-196400\">Chocolate Date Caramel Walnut Tart (Gluten-Free, Grain-Free, Vegan)</a> as a dessert. From preparation to the plate, this recipe takes roughly <b>45 minutes</b>. All things considered, we decided this recipe <b>deserves a spoonacular score of 86%</b>. This score is great. Try <a href=\"https://spoonacular.com/recipes/roasted-peppers-with-boquerones-12\">Roasted Peppers with Boquerones</a>, <a href=\"https://spoonacular.com/recipes/tomato-and-boquerones-salad-with-garlicky-breadcrumbs-49\">Tomato And Boquerones Salad With Garlicky Breadcrumbs</a>, and <a href=\"https://spoonacular.com/recipes/marinated-rib-eye-147171\">Marinated Rib Eye</a> for similar recipes.",
      author = "Delicious Days",
      cookingTime = Some(45),
      calories = Some(243),
      protein = Some(17),
      fat = Some(18),
      carbohydrates = None,
      sugar = None,
      ingredients = List(
        Ingredient(0, "bell pepper", 6, Some("servings")),
        Ingredient(0, "chillies", 1, None),
        Ingredient(0, "coarse sea salt", 6, Some("servings")),
        Ingredient(0, "extra-virgin olive oil", 6, Some("servings")),
        Ingredient(0, "fresh anchovies", 500, Some("grams")),
        Ingredient(0, "fresh parsley", 6, Some("servings")),
        Ingredient(0, "fresh thyme", 6, Some("servings")),
        Ingredient(0, "garlic cloves", 1, None),
        Ingredient(0, "juice of lemon", 1, None),
        Ingredient(0, "sherry vinegar", 5, Some("Tbsps"))
      )
    )

    val result = for {
      str <- Future(Source.fromResource("SpoonacularResponse.json").mkString)
      json <- Future(parse(str).getOrElse(Json.fromString("")))
      recipe <- SpoonacularDeserializator.fromResponse[Future](json)
    } yield recipe

    result.map(_ shouldBe recipe)
  }

  test("parse incorrect spoonacular data") {
    val result = for {
      str <- Future(Source.fromResource("SpoonacularResponse.json").mkString)
      incorrect <- Future(
        str.replace(
          """"amount": 6,
            |          "unitShort": "servings",
            |          "unitLong": "servings"""".stripMargin,
          """"ahahaha":"hahaha""""
        ))
      json <- Future(parse(incorrect).getOrElse(Json.fromString("")))
      recipe <- SpoonacularDeserializator.fromResponse[Future](json)
    } yield recipe
    recoverToSucceededIf[LoaderError.CannotParseJson](result)
  }

}
