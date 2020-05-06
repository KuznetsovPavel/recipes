package ru.pkuznetsov.bucket.dao

import doobie.implicits._

object PostgresqlBucketQueries {

  def insertBucketEntry(entry: BucketEntry) =
    sql"""
         |INSERT INTO buckets (ingredientId, amount, unit)
         |VALUES (${entry.ingredientId}, ${entry.amount}, ${entry.unit})
         |""".stripMargin.update

}
