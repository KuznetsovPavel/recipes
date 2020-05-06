package ru.pkuznetsov.bucket.model

import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf
import ru.pkuznetsov.core.model.Ingredient

final case class Bucket(ingredients: List[Ingredient])

object Bucket {
  implicit val decoder: Decoder[Bucket] = deriveDecoder[Bucket]
  implicit val encoder: Encoder[Bucket] = deriveEncoder[Bucket]

  implicit def bucketDecoder[F[_]: Sync]: EntityDecoder[F, Bucket] = jsonOf[F, Bucket]
}
