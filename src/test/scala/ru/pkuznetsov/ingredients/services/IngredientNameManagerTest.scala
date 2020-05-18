package ru.pkuznetsov.ingredients.services

import cats.data.NonEmptyList
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFunSuite, Matchers}
import ru.pkuznetsov.core.utils.FutureSyncForTest
import ru.pkuznetsov.ingredients.dao.IngredientNamesDao
import ru.pkuznetsov.ingredients.model.IngredientError._
import ru.pkuznetsov.ingredients.model.IngredientName
import ru.pkuznetsov.recipes.services.RecipeService.IngredientId

import scala.concurrent.Future

class IngredientNameManagerTest extends AsyncFunSuite with Matchers with AsyncMockFactory {

  val dao = mock[IngredientNamesDao[Future]]
  implicit val futureSync = new FutureSyncForTest().futureSync
  val manager = new IngredientNameManagerImpl[Future](dao)
  val names = List("name1", "name2", "name3")
  val ids = List(IngredientId(1), IngredientId(2), IngredientId(3))
  val ingNames = List(IngredientName(IngredientId(1), "name1"),
                      IngredientName(IngredientId(2), "name2"),
                      IngredientName(IngredientId(3), "name3"))

  test("get ingredient ids for correct names") {
    dao.getByNames _ expects NonEmptyList.fromListUnsafe(names) returns Future.successful(ingNames)
    manager.getIngredientIdsFor(names).map(names => names shouldBe ingNames)
  }

  test("get ingredient ids for incorrect names") {
    dao.getByNames _ expects NonEmptyList.fromListUnsafe(names) returns Future.successful(ingNames.drop(1))
    recoverToSucceededIf[CannotFindIngredientNames](manager.getIngredientIdsFor(names))
  }

  test("get ingredient ids for names with duplicates") {
    val nameDuplicates = names.map(_ => "name2")
    recoverToSucceededIf[IngredientNameDuplicates](manager.getIngredientIdsFor(nameDuplicates))
  }

  test("get ingredient names for correct ids") {
    dao.getByIds _ expects NonEmptyList.fromListUnsafe(ids) returns Future.successful(ingNames)
    manager.getIngredientNamesFor(ids).map(result => result shouldBe ingNames)
  }

  test("get ingredient names for incorrect ids") {
    dao.getByIds _ expects NonEmptyList.fromListUnsafe(ids) returns Future.successful(ingNames.drop(1))
    recoverToSucceededIf[CannotFindIngredientIds](manager.getIngredientNamesFor(ids))
  }

  test("get ingredient names for ids with duplicates") {
    val idDuplicates = List(IngredientId(1), IngredientId(1))
    recoverToSucceededIf[IngredientIdsDuplicates](manager.getIngredientNamesFor(idDuplicates))
  }

  test("get ingredient names for empty list") {
    recoverToSucceededIf[EmptyIngredientList.type](manager.getIngredientNamesFor(List.empty))
  }

  test("get ingredient ids for empty list") {
    recoverToSucceededIf[EmptyIngredientList.type](manager.getIngredientIdsFor(List.empty))
  }

  test("get ingredient names if all exist") {
    dao.getByNames _ expects NonEmptyList.fromListUnsafe(names) returns Future.successful(ingNames)
    manager.addAndGetNames(names).map(_ shouldBe ingNames)
  }

  test("get ingredient names if not all exist") {
    dao.getByNames _ expects NonEmptyList.fromListUnsafe(names) returns Future.successful(ingNames.drop(1))
    dao.insertIngName _ expects "name1" returns Future.successful(1)
    manager.addAndGetNames(names).map(_.sortBy(_.name)).map(_ shouldBe ingNames)
  }

}
