package ru.pkuznetsov.bucket.services

import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFunSuite, Matchers}
import ru.pkuznetsov.bucket.dao.BucketDao
import cats.instances.future._
import ru.pkuznetsov.bucket.model.BucketError.BucketNotExist
import ru.pkuznetsov.bucket.model.{Bucket, BucketEntry}
import ru.pkuznetsov.ingredients.model.IngredientError.IngredientIdsDuplicates
import ru.pkuznetsov.ingredients.services.IngredientNameManager
import ru.pkuznetsov.recipes.services.RecipeService.IngredientId

import scala.concurrent.Future

class BucketServiceTest extends AsyncFunSuite with Matchers with AsyncMockFactory {

  val bucketDao = mock[BucketDao[Future]]
  val ingNameManager = mock[IngredientNameManager[Future]]
  val bucketService = new BucketServiceImpl[Future](bucketDao, ingNameManager)

  val bucket = Bucket(
    List(
      BucketEntry(1, 1.2, Some("ml")),
      BucketEntry(2, 2.2, None),
      BucketEntry(3, 4.2, Some("ml"))
    ))

  val ingIds = List(IngredientId(1), IngredientId(2), IngredientId(3))

  test("save correct bucket") {
    ingNameManager.checkIngredientIds _ expects ingIds returns Future.unit
    bucketDao.addOrUpdateBucket _ expects bucket returns Future.unit
    bucketService.saveBucket(bucket).map(_ shouldBe ())
  }

  test("save incorrect bucket") {
    val incorrectBucket = Bucket(bucket.ingredients ++ bucket.ingredients)
    ingNameManager.checkIngredientIds _ expects (ingIds ++ ingIds) returns Future.failed(
      IngredientIdsDuplicates(List(1, 2, 3)))
    recoverToSucceededIf[IngredientIdsDuplicates](bucketService.saveBucket(incorrectBucket))
  }

  test("get bucket") {
    (bucketDao.getBucket _).expects() returns Future.successful(Some(bucket))
    bucketService.getBucket.map(_ shouldBe bucket)
  }

  test("get incorrect bucket") {
    (bucketDao.getBucket _).expects() returns Future.successful(None)
    recoverToSucceededIf[BucketNotExist.type](bucketService.getBucket)
  }

}
