package ru.pkuznetsov.bucket.dao

import cats.effect.{Bracket, Resource}
import cats.instances.list._
import cats.syntax.traverse._
import doobie.hikari.HikariTransactor
import ru.pkuznetsov.bucket.model.Bucket
import ru.pkuznetsov.core.dao.Dao

trait BucketDao[F[_]] {
  def addOrUpdateBucket(bucket: Bucket): F[Unit]
  def getBucket: F[Option[Bucket]]
}

class PostgresqlBucketDao[F[_]](transactor: Resource[F, HikariTransactor[F]])(
    implicit bracket: Bracket[F, Throwable])
    extends Dao[F](transactor)
    with BucketDao[F] {

  override def addOrUpdateBucket(bucket: Bucket): F[Unit] =
    for {
      _ <- PostgresqlBucketQueries.deleteBucketTable.run
      res <- bucket.ingredients
        .traverse(ing => PostgresqlBucketQueries.insertBucketEntry(ing).run)
        .map(_ => ())
    } yield res

  override def getBucket: F[Option[Bucket]] =
    //we read all rows from table because system work with single user (yet)
    //when multitenancy will be implemented, it should be fixed
    PostgresqlBucketQueries.selectBucket.to[List].map {
      case Nil  => None
      case list => Some(Bucket(list))
    }
}
