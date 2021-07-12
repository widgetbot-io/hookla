package venix.hookla.types.providers

import enumeratum.values._

sealed abstract class SonarrEventType(val value: String) extends StringEnumEntry

case object SonarrEventType extends StringEnum[SonarrEventType] with StringCirceEnum[SonarrEventType] {
  def values = findValues

  case object Download extends SonarrEventType("Download")
  case object Grab     extends SonarrEventType("Grab")
  case object Rename   extends SonarrEventType("Rename")
  case object Test     extends SonarrEventType("Test")
}
