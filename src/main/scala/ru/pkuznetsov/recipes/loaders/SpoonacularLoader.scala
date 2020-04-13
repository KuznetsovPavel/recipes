package ru.pkuznetsov.recipes.loaders

import cats.MonadError
import cats.effect.IO
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import io.circe.Json
import ru.pkuznetsov.recipes.loaders.SpoonacularLoader.Backend
import ru.pkuznetsov.recipes.model.Recipe
import sttp.client._
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.circe._

import scala.concurrent.ExecutionContext

class SpoonacularLoader[F[_]](backend: Backend[F], apiKey: String)
                             (implicit monad: MonadError[F, Throwable]) extends StrictLogging {

  def getRecipe(recipeId: Long): F[Recipe] = {
    val request = basicRequest
      .get(uri"https://api.spoonacular.com/recipes/$recipeId/information?apiKey=$apiKey")
      .response(asJson[Json])

    for {
      response <- backend.send(request)
      recipeJson <-  monad.fromEither(response.body)
      _ <- monad.pure(logger.debug(s"get recipe with id $recipeId, content: $recipeJson"))
      recipe <- Recipe.fromSpoonacularResponse[F](recipeJson)
    } yield recipe

  }
}

object SpoonacularLoader {
  type Backend[F[_]] = SttpBackend[F, Nothing, Nothing]
}

object Test extends App {
  implicit val cs = IO.contextShift(ExecutionContext.global)

  val res = for {
    backend <- AsyncHttpClientCatsBackend[IO]()
    loader = new SpoonacularLoader(backend, "9be944945f3548d4854ea918c6e13963")
    res <- loader.getRecipe(333L)
    _ <- backend.close()
  } yield res

  println(res.unsafeRunSync())
}
