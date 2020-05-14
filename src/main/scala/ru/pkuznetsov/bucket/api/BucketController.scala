package ru.pkuznetsov.bucket.api

import cats.effect.{ContextShift, Sync}
import cats.syntax.semigroupk._
import cats.{Applicative, Defer, MonadError}
import org.http4s.HttpRoutes
import ru.pkuznetsov.bucket.model.Bucket
import ru.pkuznetsov.bucket.services.BucketService
import ru.pkuznetsov.core.api.Http4sController
import ru.pkuznetsov.core.api.TapirJsonCirceImpl._
import ru.pkuznetsov.core.model.ErrorData
import sttp.tapir.endpoint
import sttp.tapir.server.http4s._

class BucketController[F[_]: Applicative: Defer: Sync](service: BucketService[F])(
    implicit monad: MonadError[F, Throwable],
    cs: ContextShift[F])
    extends Http4sController[F] {

  val saveBucket = endpoint.post.in("bucket").in(jsonBody[Bucket]).errorOut(jsonBody[ErrorData])
  val getBucket = endpoint.get.in("bucket").out(jsonBody[Bucket]).errorOut(jsonBody[ErrorData])

  val saveBucketRoutes = saveBucket.toRoutes(bucket => service.saveBucket(bucket).toTapirResponse)

  val getBucketRoutes = getBucket.toRoutes(_ => service.getBucket.toTapirResponse)

  override val endpoints = List(saveBucket, getBucket)

  override val routes: HttpRoutes[F] = saveBucketRoutes <+> getBucketRoutes

}
