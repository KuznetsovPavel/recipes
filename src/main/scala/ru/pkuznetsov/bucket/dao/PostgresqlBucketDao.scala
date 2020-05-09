package ru.pkuznetsov.bucket.dao

import cats.effect.{Bracket, Resource}
import doobie.hikari.HikariTransactor
import ru.pkuznetsov.bucket.model.Bucket
import cats.syntax.traverse._
import cats.instances.list._
import ru.pkuznetsov.core.dao.Dao

trait BucketDao[F[_]] {
  def addOrUpdateBucket(bucket: Bucket): F[Unit]
}

class PostgresqlBucketDao[F[_]](transactor: Resource[F, HikariTransactor[F]])(
    implicit bracket: Bracket[F, Throwable])
    extends Dao[F](transactor)
    with BucketDao[F] {

  def addOrUpdateBucket(bucket: Bucket): F[Unit] =
    for {
      _ <- PostgresqlBucketQueries.deleteBucketTable.run
      res <- bucket.ingredients
        .traverse(ing => PostgresqlBucketQueries.insertBucketEntry(ing).run)
        .map(_ => ())
    } yield res

}
