package ru.pkuznetsov.bucket.dao

final case class BucketEntry(ingredientId: Int, amount: Double, `unit`: Option[String])
