package ru.pkuznetsov.bucket.dao

import cats.effect.{Bracket, Resource}
import doobie.hikari.HikariTransactor
import ru.pkuznetsov.bucket.model.Bucket

class PostgresqlBucketDao[F[_]](transactor: Resource[F, HikariTransactor[F]])(
    implicit bracket: Bracket[F, Throwable]) {

  def addOrUpdateBucket(bucket: Bucket): F[Unit] = ???
//    bucket.ingredients
//      .traverse(ing => PostgresqlBucketQueries.insertBucketEntry(ing).run)
//      .map(_ => ())

}
