package ru.pkuznetsov.bucket.api

import cats.effect.Sync
import cats.{Applicative, Defer}

class BucketController[F[_]: Applicative: Defer: Sync] {}
