package ru.pkuznetsov.bucket.services

import ru.pkuznetsov.bucket.dao.PostgresqlBucketDao
import ru.pkuznetsov.bucket.model.Bucket

class BucketService[F[_]](dao: PostgresqlBucketDao[F]) {

  def saveBucket(bucket: Bucket): F[Unit] = dao.addOrUpdateBucket(bucket)

}
