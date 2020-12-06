package venix.hookla.providers

trait BaseProvider {
  def id: String
  def name: String
  def logo: String

  def eventHeader: Option[String] = None
  def tokenHeader: Option[String] = None
  def eventBodyKey: Option[String] = None
  def tokenBodyKey: Option[String] = None


  def events: List[Nothing] // What type innit
}

