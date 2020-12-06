package venix.hookla.providers

trait BaseProvider {
  def name: String
  def logo: String
  def eventHeader: Option[String]
  def tokenHeader: Option[String]

  def events: List[Nothing] // What type innit
}

