package ru.pkuznetsov.recipes.loaders

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.util.ByteString
import cats.effect.{ContextShift, IO}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

class SpoonacularLoader(apiKey: String) extends StrictLogging {

  def getRecipe(recipeId: Long)(implicit ac: ActorSystem, cs: ContextShift[IO]) =
    for {
      uri <- IO.pure(s"https://api.spoonacular.com/recipes/$recipeId/information?apiKey=$apiKey")
      response <- IO.fromFuture(IO(Http().singleRequest(HttpRequest(uri = uri, method = HttpMethods.GET))))
      entity <- IO.fromFuture(IO(response.entity.dataBytes.runFold(ByteString(""))(_ ++ _)))
      stringEntity <- IO(entity.utf8String)
      _ <- IO(logger.debug(s"get recipe with id $recipeId, content: $stringEntity"))
    } yield stringEntity
}

object Test extends App {
  implicit val system = ActorSystem()
  implicit val cs = IO.contextShift(ExecutionContext.global)

  val loader = new SpoonacularLoader("9be944945f3548d4854ea918c6e13963")
  loader.getRecipe(10).unsafeRunSync()
  Await.result(system.terminate(), Duration.Inf)
}
