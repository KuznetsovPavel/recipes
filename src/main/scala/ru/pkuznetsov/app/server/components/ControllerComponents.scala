package ru.pkuznetsov.app.server.components

import cats.effect.{Async, ContextShift, Resource, Sync}
import com.softwaremill.macwire.wire
import ru.pkuznetsov.app.utill.{PostgresConfig, PsqlIOTranscator}
import ru.pkuznetsov.bucket.api.BucketController
import ru.pkuznetsov.bucket.dao.PostgresqlBucketDao
import ru.pkuznetsov.bucket.services.BucketServiceImpl
import ru.pkuznetsov.core.api.Http4sController
import ru.pkuznetsov.ingredients.api.IngredientNamesController
import ru.pkuznetsov.ingredients.dao.PostgresqlIngredientNamesDao
import ru.pkuznetsov.ingredients.model.IngredientNameServiceImpl
import ru.pkuznetsov.ingredients.services.IngredientNameManagerImpl
import ru.pkuznetsov.recipes.api.RecipeController
import ru.pkuznetsov.recipes.dao.PostgresqlRecipeDao
import ru.pkuznetsov.recipes.services.{RecipeServiceImpl, RecipeTableManagerImpl}

object ControllerComponents {

  def apply[F[_]: Sync: Async: ContextShift](config: PostgresConfig): Resource[F, List[Http4sController[F]]] =
    Resource.liftF {
      Sync[F].delay {
        val transactor = PsqlIOTranscator.create(config.user, config.password, config.url, config.threads)
        val recipeDao = wire[PostgresqlRecipeDao[F]]
        val ingredientNamesDao = wire[PostgresqlIngredientNamesDao[F]]
        val ingredientNameManager = wire[IngredientNameManagerImpl[F]]
        val recipeTableManager = wire[RecipeTableManagerImpl[F]]
        val bucketDao = wire[PostgresqlBucketDao[F]]
        val recipeService = wire[RecipeServiceImpl[F]]
        val bucketService = wire[BucketServiceImpl[F]]
        val ingNameService = wire[IngredientNameServiceImpl[F]]

        val ingredients = wire[IngredientNamesController[F]]
        val recipe = wire[RecipeController[F]]
        val bucket = wire[BucketController[F]]

        List(recipe, bucket, ingredients)
      }
    }

}
