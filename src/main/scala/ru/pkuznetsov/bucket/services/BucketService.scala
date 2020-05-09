package ru.pkuznetsov.bucket.services

import cats.MonadError
import cats.syntax.flatMap._
import cats.syntax.functor._
import ru.pkuznetsov.bucket.dao.BucketDao
import ru.pkuznetsov.bucket.model.Bucket
import ru.pkuznetsov.bucket.model.BucketError.BucketNotExist
import ru.pkuznetsov.ingredients.services.IngredientNameManager
import ru.pkuznetsov.recipes.services.RecipeService.IngredientId

trait BucketService[F[_]] {
  def saveBucket(bucket: Bucket): F[Unit]
  def getBucket: F[Bucket]
}

class BucketServiceImpl[F[_]](bucketDao: BucketDao[F], ingredientNameManager: IngredientNameManager[F])(
    implicit monad: MonadError[F, Throwable])
    extends BucketService[F] {

  def saveBucket(bucket: Bucket): F[Unit] =
    for {
      ingIds <- monad.pure(bucket.ingredients.map(ing => IngredientId(ing.ingredientId)))
      _ <- ingredientNameManager.checkIngredientIds(ingIds)
      result <- bucketDao.addOrUpdateBucket(bucket)
    } yield result

  def getBucket: F[Bucket] =
    for {
      bucketOpt <- bucketDao.selectBucket
      bucket <- monad.fromOption(bucketOpt, BucketNotExist)
    } yield bucket

}
