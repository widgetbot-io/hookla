package venix.hookla.actors

case class Provider (
  id: String,
  name: String,
  logo: String,
  eventHeader: Option[String] = None,
  eventBodyKey: Option[String] = None,
)
