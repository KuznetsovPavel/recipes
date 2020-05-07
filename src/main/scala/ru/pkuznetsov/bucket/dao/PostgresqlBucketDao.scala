package ru.pkuznetsov.bucket.dao

import cats.effect.{Bracket, Resource}
import cats.instances.list._
import cats.syntax.traverse._
import doobie.hikari.HikariTransactor
import ru.pkuznetsov.bucket.model.Bucket
import ru.pkuznetsov.ingredients.dao.PostgresqlIngredientNamesDao

class PostgresqlBucketDao[F[_]](transactor: Resource[F, HikariTransactor[F]])(
    implicit bracket: Bracket[F, Throwable])
    extends PostgresqlIngredientNamesDao[F](transactor) {

  def addOrUpdateBucket(bucket: Bucket): F[Unit] =
    bucket.ingredients
      .traverse(ing => PostgresqlBucketQueries.insertBucketEntry(ing).run)
      .map(_ => ())

}
