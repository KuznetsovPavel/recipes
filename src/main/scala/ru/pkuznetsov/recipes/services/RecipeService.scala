package ru.pkuznetsov.recipes.services

import cats.MonadError
import cats.data.NonEmptyList
import cats.syntax.flatMap._
import cats.syntax.functor._
import ru.pkuznetsov.core.model.Errors.{
  CannotFindIngredients,
  IngredientDuplicates,
  RecipeShouldHaveIngredients
}
import ru.pkuznetsov.ingredients.dao.PostgresqlIngredientNamesDao
import ru.pkuznetsov.recipes.dao.PostgresqlRecipeDao
import ru.pkuznetsov.recipes.model.Recipe
import ru.pkuznetsov.recipes.services.RecipeService.RecipeId
import supertagged.TaggedType

trait RecipeService[F[_]] {
  def save(recipe: Recipe): F[Int]
  def get(id: RecipeId): F[Recipe]
}

class RecipeServiceImpl[F[_]](
    recipeDao: PostgresqlRecipeDao[F],
    ingredientNameDao: PostgresqlIngredientNamesDao[F])(implicit monad: MonadError[F, Throwable])
    extends RecipeService[F] {

  def save(recipe: Recipe) =
    for {
      ingredients <- monad.pure(recipe.ingredients.map(_.name))

      nonEmptyIngredients <- monad.fromOption(NonEmptyList.fromList(ingredients),
                                              RecipeShouldHaveIngredients(recipe))

      uniq <- monad.pure(ingredients.diff(ingredients.distinct)).flatMap {
        case Nil  => monad.pure(nonEmptyIngredients)
        case list => monad.raiseError[NonEmptyList[String]](IngredientDuplicates(list))
      }

      _ <- ingredientNameDao
        .getByNames(uniq)
        .flatMap {
          case list if list.size != uniq.size =>
            val incorrect = uniq.toList.diff(list.map(_._2))
            monad.raiseError[List[(Int, String)]](CannotFindIngredients(incorrect))
          case list => monad.pure(list)
        }
        .map(_.reverse.toMap)

      result <- recipeDao.insertRecipe(recipe)
    } yield result

  def get(id: RecipeId): F[Recipe] = recipeDao.selectRecipe(id)

}

object RecipeService {
  object RecipeId extends TaggedType[Int]
  type RecipeId = RecipeId.Type
}
