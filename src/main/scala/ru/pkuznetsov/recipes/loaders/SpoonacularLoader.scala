package ru.pkuznetsov.recipes.loaders

import cats.MonadError
import cats.effect.{ExitCode, IO, IOApp}
import cats.instances.list._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.monadError._
import cats.syntax.traverse._
import com.typesafe.scalalogging.StrictLogging
import io.circe.Json
import ru.pkuznetsov.recipes.loaders.SpoonacularLoader.{Backend, SpoonacularRecipeId}
import ru.pkuznetsov.recipes.model.Errors.SpoonacularError
import ru.pkuznetsov.recipes.model.Recipe
import sttp.client._
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.circe._
import supertagged.TaggedType

class SpoonacularLoader[F[_]](backend: Backend[F], apiKey: String)(implicit monad: MonadError[F, Throwable])
    extends StrictLogging {

  def getRecipe(recipeId: SpoonacularRecipeId): F[Recipe] = {
    val request = basicRequest
      .get(uri"https://api.spoonacular.com/recipes/$recipeId/information?apiKey=$apiKey")
      .response(asJson[Json])

    for {
      response <- backend.send(request)
      recipeJson <- monad
        .fromEither(response.body)
        .adaptError { case ex => SpoonacularError(ex) }
      _ <- monad.pure(logger.debug(s"get recipe with id $recipeId, content: $recipeJson"))
      recipe <- Recipe.fromSpoonacularResponse[F](recipeJson)
    } yield recipe

  }
}

object SpoonacularLoader {
  type Backend[F[_]] = SttpBackend[F, Nothing, Nothing]
  object SpoonacularRecipeId extends TaggedType[Long]
  type SpoonacularRecipeId = SpoonacularRecipeId.Type
}

object Test extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    for {
      backend <- AsyncHttpClientCatsBackend[IO]()
      loader <- IO(new SpoonacularLoader(backend, "9be944945f3548d4854ea918c6e13963"))
      range <- IO(100L to 300L by 100).map(SpoonacularRecipeId(_))
      recipes <- range.toList
        .traverse(loader.getRecipe(_).map(Option(_)).handleError(_ => None))
      measures <- IO(recipes.flatten.flatMap(_.ingredients).flatMap(_.unit))
      _ <- backend.close()
      _ <- IO(println(measures.distinct.mkString(", ")))
      // grams, large, servings, Tbsp, milliliters, teaspoons, heads, slices, fillets, ball
      // large head, Tbsps, pinch, handful, clove, fillet, liters, cloves, head, serving
      // stick, leaves, sprigs, large heads, teaspoon, smalls, Tb, bunch, sheet
    } yield ExitCode.Success
}
