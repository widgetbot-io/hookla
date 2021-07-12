package venix.hookla.types

case class Provider(
    id: String,
    name: String,
    logo: String,
    eventKey: String,
    isBody: Boolean = false,
)
