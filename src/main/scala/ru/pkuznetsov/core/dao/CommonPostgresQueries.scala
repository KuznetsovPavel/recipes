package ru.pkuznetsov.core.dao

import doobie.implicits._

object CommonPostgresQueries {

  def insertIngredientName(name: String) =
    sql"""
         |INSERT INTO ingredient_names (ingredient)
         |VALUES ($name)
         |""".stripMargin.update

  def selectIngredientNameId(name: String) = {
    sql"""
         |SELECT id FROM ingredient_names
         |WHERE  ingredient = ${name}
         |""".stripMargin.query[Int]
  }

  def selectIngredientName(id: Int) = {
    sql"""
         |SELECT id, ingredient FROM ingredient_names
         |WHERE  id = ${id}
         |""".stripMargin.query[(Int, String)]
  }

}
