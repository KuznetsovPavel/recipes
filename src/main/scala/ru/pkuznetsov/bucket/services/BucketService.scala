package ru.pkuznetsov.bucket.services

import cats.MonadError
import ru.pkuznetsov.bucket.dao.PostgresqlBucketDao
import ru.pkuznetsov.bucket.model.Bucket
import ru.pkuznetsov.ingredients.dao.IngredientNamesDao

class BucketService[F[_]](bucketDao: PostgresqlBucketDao[F], ingredientNameDao: IngredientNamesDao[F])(
    implicit monad: MonadError[F, Throwable]) {

  def saveBucket(bucket: Bucket): F[Unit] = ???
//    for {
//      ids <- NonEmptyList.fromList(bucket.ingredients.map(_.id)) match {
//        case Some(list) => monad.pure(list)
//        case None       => monad.raiseError[NonEmptyList[Int]](EmptyBucket)
//      }
//
//      names <- ingredientNameDao.getByIds(ids)
//      _ <- ids.toList.diff(names.map(_._1)) match {
//        case Nil  => monad.pure()
//        case list => monad.raiseError[Unit](IngredientDuplicates(list.map(_.toString)))
//      }
//
//      result <- bucketDao.addOrUpdateBucket(bucket)
//    } yield result

}
