package ru.pkuznetsov.core.api

import io.circe.Printer
import sttp.tapir.json.circe.TapirJsonCirce

object TapirJsonCirceImpl extends TapirJsonCirce {
  override val jsonPrinter = Printer.noSpaces.copy(dropNullValues = true)
}
