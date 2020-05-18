package ru.pkuznetsov.app.loader.components

import cats.effect.{Concurrent, ContextShift, Resource}
import com.softwaremill.macwire.wire
import ru.pkuznetsov.app.loader.components.ConfigComponent.LoaderConfig
import ru.pkuznetsov.app.utill.PsqlIOTranscator
import ru.pkuznetsov.ingredients.dao.PostgresqlIngredientNamesDao
import ru.pkuznetsov.ingredients.services.IngredientNameManagerImpl
import ru.pkuznetsov.loaders.connectors.spoonacular.SpoonacularLoader
import ru.pkuznetsov.loaders.connectors.spoonacular.SpoonacularLoader.SpoonacularApiKey
import ru.pkuznetsov.loaders.services.LoaderService
import ru.pkuznetsov.recipes.dao.PostgresqlRecipeDao
import ru.pkuznetsov.recipes.services.RecipeTableManagerImpl
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend

object ServiceComponent {

  def apply[F[_]: ContextShift: Concurrent](config: LoaderConfig): Resource[F, LoaderService[F]] = {
    Resource.make(AsyncHttpClientCatsBackend[F]())(_.close()).flatMap { backend =>
      Resource.pure {
        val transactor =
          PsqlIOTranscator.create(config.database.user,
                                  config.database.password,
                                  config.database.url,
                                  config.database.threads)
        val loader = new SpoonacularLoader(backend, SpoonacularApiKey(config.spoonacular.apiKey))
        val recipeDao = wire[PostgresqlRecipeDao[F]]
        val ingNameDao = wire[PostgresqlIngredientNamesDao[F]]
        val nameManager = wire[IngredientNameManagerImpl[F]]
        val recipeTableManager = wire[RecipeTableManagerImpl[F]]
        wire[LoaderService[F]]
      }
    }
  }
}
