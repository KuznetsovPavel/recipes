package ru.pkuznetsov.bucket.dao

import cats.effect.Resource
import doobie.hikari.HikariTransactor

class PostgresqlBucketDao[F[_]](transactor: Resource[F, HikariTransactor[F]]) {}
