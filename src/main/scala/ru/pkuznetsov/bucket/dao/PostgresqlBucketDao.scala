package ru.pkuznetsov.bucket.dao

import cats.effect.{Bracket, Resource}
import cats.instances.list._
import cats.syntax.traverse._
import doobie.hikari.HikariTransactor
import ru.pkuznetsov.bucket.model.Bucket
import ru.pkuznetsov.core.dao.Dao

class PostgresqlBucketDao[F[_]](transactor: Resource[F, HikariTransactor[F]])(
    implicit bracket: Bracket[F, Throwable])
    extends Dao[F](transactor) {

  def addOrUpdateBucket(bucket: Bucket): F[Unit] = {
    def insertBucketEntry(entry: BucketEntry) =
      PostgresqlBucketQueries.insertBucketEntry(entry).run

    bucket.ingredients
      .traverse { ing =>
        checkIngNameAnd(ing)(id => insertBucketEntry(BucketEntry(id, ing.amount, ing.unit)))
      }
      .map(_ => ())
  }

}
