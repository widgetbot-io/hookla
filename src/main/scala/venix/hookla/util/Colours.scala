package venix.hookla.util

object Colours {
  private type Colour = Int

  val PUSH: Colour = 0x0DA2FF
  val NOTE: Colour = 0xFFA500

  val CREATED: Colour = 0x08000
  val DELETED: Colour = 0xFF0000

  val FAILED: Colour   = 0xFF0000
  val CANCELED: Colour = 0xFFFF00
  val RUNNING: Colour  = 0xE89D13
  val SUCCESS: Colour  = 0x30FF49
}
