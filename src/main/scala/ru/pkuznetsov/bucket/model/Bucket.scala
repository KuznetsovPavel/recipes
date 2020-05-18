package ru.pkuznetsov.bucket.model

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

final case class Bucket(ingredients: List[BucketEntry])

final case class BucketEntry(ingredientId: Int, amount: Double, unit: Option[String])

object Bucket {
  implicit val decoder: Decoder[Bucket] = deriveDecoder[Bucket]
  implicit val encoder: Encoder[Bucket] = deriveEncoder[Bucket]
}

object BucketEntry {
  implicit val decoder: Decoder[BucketEntry] = deriveDecoder[BucketEntry]
  implicit val encoder: Encoder[BucketEntry] = deriveEncoder[BucketEntry]
}
