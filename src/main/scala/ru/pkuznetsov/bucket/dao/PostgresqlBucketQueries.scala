package ru.pkuznetsov.bucket.dao

import doobie.implicits._
import ru.pkuznetsov.bucket.model.BucketEntry

object PostgresqlBucketQueries {

  def insertBucketEntry(entry: BucketEntry) =
    sql"""
         |INSERT INTO buckets (ingredientId, amount, unit)
         |VALUES (${entry.id}, ${entry.amount}, ${entry.unit})
         |""".stripMargin.update

}
