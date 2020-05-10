package ru.pkuznetsov.app

import cats.effect.{ExitCode, IO, IOApp}
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import cats.syntax.traverse._
import cats.instances.list._
import cats.syntax.applicativeError._
import ru.pkuznetsov.loaders.spoonacular.SpoonacularLoader

object LoaderRunner extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = ???
//    for {
//      backend <- AsyncHttpClientCatsBackend[IO]()
//      loader <- IO(new SpoonacularLoader(backend, "9be944945f3548d4854ea918c6e13963"))
//      range <- IO(100L to 300L by 100).map(SpoonacularRecipeId(_))
//      recipes <- range.toList
//        .traverse(loader.getRecipe(_).map(Option(_)).handleError(_ => None))
//      measures <- IO(recipes.flatten.flatMap(_.ingredients).flatMap(_.unit))
//      _ <- backend.close()
//      _ <- IO(println(measures.distinct.mkString(", ")))
//      // grams, large, servings, Tbsp, milliliters, teaspoons, heads, slices, fillets, ball
//      // large head, Tbsps, pinch, handful, clove, fillet, liters, cloves, head, serving
//      // stick, leaves, sprigs, large heads, teaspoon, smalls, Tb, bunch, sheet
//    } yield ExitCode.Success
}
