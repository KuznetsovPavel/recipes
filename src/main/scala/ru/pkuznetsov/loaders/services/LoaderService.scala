package ru.pkuznetsov.loaders.services

import cats.MonadError
import cats.instances.list._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import com.typesafe.scalalogging.StrictLogging
import ru.pkuznetsov.ingredients.model.IngredientName
import ru.pkuznetsov.ingredients.services.IngredientNameManager
import ru.pkuznetsov.loaders.connectors.spoonacular.RecipeLoader
import ru.pkuznetsov.loaders.connectors.spoonacular.SpoonacularLoader.LoaderRecipeId
import ru.pkuznetsov.loaders.model.LoaderError.{CannotFindIngredient, DuplicateIngredients}
import ru.pkuznetsov.recipes.dao.{IngredientRow, RecipeDao}
import ru.pkuznetsov.recipes.model.Ingredient
import ru.pkuznetsov.recipes.services.RecipeService.RecipeId
import ru.pkuznetsov.recipes.services.RecipeTableManager

class LoaderService[F[_]](loader: RecipeLoader[F],
                          recipeDao: RecipeDao[F],
                          ingredientNameManager: IngredientNameManager[F],
                          recipeTableManager: RecipeTableManager[F])(implicit monad: MonadError[F, Throwable])
    extends StrictLogging {

  def loadAndSave(id: LoaderRecipeId): F[RecipeId] = {
    def ings2ingRowsWithIds(ings: List[Ingredient], names: List[IngredientName]): F[List[IngredientRow]] =
      ings.traverse { ingredient =>
        names.find(_.name == ingredient.name).map(_.id) match {
          case Some(id) => monad.pure(recipeTableManager.ing2IngRow(ingredient.copy(id = id)))
          case None     => monad.raiseError[IngredientRow](CannotFindIngredient(ingredient.name))
        }
      }

    def unionSameIngredients(ingredients: List[Ingredient]): F[List[Ingredient]] = {
      def checkSameIngs(name: String, list: List[Ingredient]): F[Ingredient] =
        if (list.map(_.unit).distinct == 1)
          monad.pure(list.reduce((a, b) => a.copy(amount = a.amount + b.amount)))
        else monad.raiseError[Ingredient](DuplicateIngredients(id, name))

      ingredients.groupBy(_.name).toList.traverse {
        case (name, ings) =>
          ings match {
            case List(i) => monad.pure(i)
            case list    => checkSameIngs(name, list)
          }
      }
    }

    for {
      _ <- monad.pure(logger.debug(s"loading recipe with id ${id} from Spoonacular"))
      recipe <- loader.getRecipe(id)
      recipeRow <- monad.pure(recipeTableManager.recipe2RecipeRow(recipe))
      checkedIngs <- unionSameIngredients(recipe.ingredients)
      names <- ingredientNameManager.addAndGetNames(checkedIngs.map(_.name))
      ingRows <- ings2ingRowsWithIds(checkedIngs, names)
      result <- recipeDao.saveRecipeWithIngredients(recipeRow, ingRows)
      _ <- monad.pure(logger.debug(s"recipe with id ${id} was loaded succesfully"))
    } yield result
  }

}
