package ru.pkuznetsov.ingredients.dao

import cats.data.NonEmptyList
import doobie._
import doobie.implicits._

object IngredientNamesPostgresQueries {

  def insert(name: String) =
    sql"""
         |INSERT INTO ingredient_names (ingredient)
         |VALUES ($name)
         |""".stripMargin.update

  def selectById(id: Int) =
    sql"""
         |SELECT id, ingredient FROM ingredient_names
         |WHERE  id = $id
         |""".stripMargin.query[(Int, String)]

  def selectAll =
    sql"""
         |SELECT id, ingredient FROM ingredient_names
         |""".stripMargin.query[(Int, String)]

  def selectByIds(ids: NonEmptyList[Int]) = {
    val first = fr"""
         |SELECT id, ingredient FROM ingredient_names
         |WHERE  """.stripMargin
    val second = Fragments.in(fr"id", ids)
    (first ++ second).query[(Int, String)]
  }

  def selectByNames(names: NonEmptyList[String]) = {
    val first = fr"""
                    |SELECT id, ingredient FROM ingredient_names
                    |WHERE  """.stripMargin
    val second = Fragments.in(fr"ingredient", names)
    (first ++ second).query[(Int, String)]
  }
}
