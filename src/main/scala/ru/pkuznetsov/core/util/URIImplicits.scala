package ru.pkuznetsov.core.util

import java.net.URI

import io.circe.{Decoder, Encoder, Json}

import scala.util.Try

object URIImplicits {
  implicit val uriDecoder: Decoder[URI] = Decoder.decodeString.emapTry { str =>
    Try(URI.create(str))
  }

  implicit val uriEncoder: Encoder[URI] = (uri: URI) => Json.fromString(uri.toString)
}
