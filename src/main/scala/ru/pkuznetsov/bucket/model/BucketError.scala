package ru.pkuznetsov.bucket.model

import ru.pkuznetsov.core.model.AppError

sealed trait BucketError extends AppError

object BucketError {
  object BucketNotExist extends BucketError

  def handleError =
    (e: BucketError) =>
      e match {
        case BucketNotExist => "bucket not exist"
    }
}
