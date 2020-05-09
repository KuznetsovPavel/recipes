package ru.pkuznetsov.bucket.api

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Applicative, Defer}
import io.circe.syntax._
import org.http4s.{HttpRoutes, Request, Response}
import ru.pkuznetsov.bucket.model.Bucket
import ru.pkuznetsov.bucket.services.BucketService
import ru.pkuznetsov.core.api.Http4sController

class BucketController[F[_]: Applicative: Defer: Sync](service: BucketService[F])
    extends Http4sController[F] {

  override val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case bucket @ POST -> Root => saveBucket(bucket)
  }

  private def saveBucket(request: Request[F]): F[Response[F]] = {
    val res = for {
      bucket <- request.as[Bucket]
      result <- service.saveBucket(bucket)
    } yield result.asJson
    checkErrorAndReturn(res)
  }

}
