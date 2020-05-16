package ru.pkuznetsov.ingredients.model

import ru.pkuznetsov.ingredients.dao.IngredientNamesDao

trait IngredientNameService[F[_]] {
  def getIngredientNames: F[List[IngredientName]]
}

class IngredientNameServiceImpl[F[_]](dao: IngredientNamesDao[F]) extends IngredientNameService[F] {
  override def getIngredientNames: F[List[IngredientName]] = dao.getAll
}
