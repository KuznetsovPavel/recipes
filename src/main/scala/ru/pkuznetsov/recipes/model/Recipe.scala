package ru.pkuznetsov.recipes.model

import java.net.URI

import cats.MonadError
import cats.effect.Sync
import cats.instances.option._
import cats.syntax.applicativeError._
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder, Json}
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf
import ru.pkuznetsov.core.model.Ingredient

import scala.util.Try

final case class Recipe(id: Int,
                        name: String,
                        uri: Option[URI],
                        summary: String,
                        author: String,
                        cookingTime: Option[Int],
                        calories: Option[Double],
                        protein: Option[Double],
                        fat: Option[Double],
                        carbohydrates: Option[Double],
                        sugar: Option[Double],
                        ingredients: List[Ingredient])

object Recipe {
  implicit def recipeDecoder[F[_]: Sync]: EntityDecoder[F, Recipe] = jsonOf[F, Recipe]

  implicit val decoder: Decoder[Recipe] = deriveDecoder[Recipe]
  implicit val encoder: Encoder[Recipe] = deriveEncoder[Recipe]

  implicit val uriDecoder: Decoder[URI] = Decoder.decodeString.emapTry { str =>
    Try(URI.create(str))
  }

  implicit val uriEncoder: Encoder[URI] = (uri: URI) => Json.fromString(uri.toString)

  def fromSpoonacularResponse[F[_]](json: Json)(implicit monad: MonadError[F, Throwable]): F[Recipe] =
    monad
      .catchNonFatal {
        def getOpt[A](name: String): Option[Json] = (json \\ name).headOption

        def getSummaryData(postfix: String): Option[Double] =
          getOpt("summary").flatMap(_.asString).flatMap { str =>
            val num = str.split(postfix)(0).split("<b>").last
            Try(num.toDouble).toOption match {
              case Some(0) | None => None
              case Some(v)        => Some(v)
            }
          }

        def parseIngredients: List[Ingredient] =
          (json \\ "extendedIngredients")
            .flatMap { json =>
              json.asArray.toList.flatten
            }
            .map { json =>
              Ingredient(
                id = 0L,
                name = (json \\ "name").headOption.flatMap(_.asString).get,
                amount = (json \\ "metric").flatMap(_ \\ "amount").flatMap(_.asNumber).map(_.toDouble).head,
                unit = (json \\ "metric").flatMap(_ \\ "unitLong").flatMap(_.asString).headOption.flatMap {
                  case "" => None
                  case v  => Some(v)
                }
              )
            }

        Recipe(
          id = 0,
          name = getOpt("title").flatMap(_.asString).get,
          uri = getOpt("sourceUrl").flatMap(_.asString).map(URI.create),
          summary = getOpt("summary")
            .flatMap(_.asString)
            .recoverWith { _ =>
              getOpt("spoonacularSourceUrl").flatMap(_.asString)
            }
            .get,
          author = getOpt("sourceName").flatMap(_.asString).getOrElse("Spoonacular api"),
          cookingTime = getOpt("cookingMinutes").flatMap(_.asNumber).flatMap(_.toInt).flatMap {
            case 0 => getOpt("readyInMinutes").flatMap(_.asNumber).flatMap(_.toInt)
            case v => Some(v)
          },
          calories = getSummaryData(" calories</b>"),
          protein = getSummaryData("g of protein</b>"),
          fat = getSummaryData("g of fat</b>"),
          carbohydrates = None,
          sugar = None,
          ingredients = parseIngredients
        )
      }
      .handleErrorWith { err =>
        monad.raiseError(Errors.CannotParseData(err))
      }

}
