package venix.hookla.providers

trait BaseProvider {
  def name: String
  def logo: String
  def eventHeader: Option[String] = None
  def tokenHeader: Option[String] = None

  def events: List[BaseEvent]
}

