package ru.pkuznetsov.bucket.dao

import cats.effect.{Bracket, Resource}
import doobie.hikari.HikariTransactor
import ru.pkuznetsov.core.dao.Dao

class PostgresqlBucketDao[F[_]](transactor: Resource[F, HikariTransactor[F]])(
    implicit bracket: Bracket[F, Throwable])
    extends Dao[F](transactor) {

//  def addOrUpdateBucket(bucket: Bucket): F[Int] =
//    bucket.ingredients

}
