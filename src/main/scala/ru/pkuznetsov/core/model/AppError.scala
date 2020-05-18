package ru.pkuznetsov.core.model

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import ru.pkuznetsov.bucket.model.BucketError
import ru.pkuznetsov.ingredients.model.IngredientError
import ru.pkuznetsov.recipes.model.RecipeError

trait AppError extends Throwable

final case class ErrorData(error: String)

object ErrorData {
  implicit val decoder: Decoder[ErrorData] = deriveDecoder[ErrorData]
  implicit val encoder: Encoder[ErrorData] = deriveEncoder[ErrorData]
}

object AppError {
  def handleError(ex: Throwable): ErrorData = ex match {
    case e: RecipeError     => ErrorData(RecipeError.handleError(e))
    case e: IngredientError => ErrorData(IngredientError.handleError(e))
    case e: BucketError     => ErrorData(BucketError.handleError(e))
    case _                  => ErrorData("internal error")
  }
}
