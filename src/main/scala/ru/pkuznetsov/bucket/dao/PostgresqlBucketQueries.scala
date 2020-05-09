package ru.pkuznetsov.bucket.dao

import doobie.implicits._
import ru.pkuznetsov.bucket.model.BucketEntry

object PostgresqlBucketQueries {

  def insertBucketEntry(entry: BucketEntry) =
    sql"""
         |INSERT INTO buckets (ingredientId, amount, unit)
         |VALUES (${entry.ingredientId}, ${entry.amount}, ${entry.unit})
         |""".stripMargin.update

  def deleteBucketTable =
    sql"""
         |DELETE FROM buckets
         |""".stripMargin.update

}
