package ru.pkuznetsov.recipes.model

sealed trait Errors extends Throwable

object Errors {
  final case class CannotParseData(cause: Throwable) extends Errors
}
