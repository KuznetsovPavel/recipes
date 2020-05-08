package ru.pkuznetsov.core.model

trait AppError extends Throwable

object AppError {
  final case class CannotParseData(cause: Throwable) extends AppError
  final case class SpoonacularError(cause: Throwable) extends AppError
  final object EmptyBucket extends AppError
}
