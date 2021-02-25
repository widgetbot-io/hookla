package venix.hookla.types

import enumeratum.values._

sealed abstract class GithubCheckRunAction(val value: String) extends StringEnumEntry
case object GithubCheckRunAction extends StringEnum[GithubCheckRunAction] with StringCirceEnum[GithubCheckRunAction] {
  def values = findValues

  case object Created         extends GithubCheckRunAction("created")
  case object Completed       extends GithubCheckRunAction("completed")
  case object ReRequested     extends GithubCheckRunAction("rerequested")
  case object RequestedAction extends GithubCheckRunAction("requested_action")
}

sealed abstract class GithubCheckRunStatus(val value: String) extends StringEnumEntry
case object GithubCheckRunStatus extends StringEnum[GithubCheckRunStatus] with StringCirceEnum[GithubCheckRunStatus] {
  def values = findValues

  case object Queued     extends GithubCheckRunStatus("queued")
  case object InProgress extends GithubCheckRunStatus("in_progress")
  case object Completed  extends GithubCheckRunStatus("completed")
}

sealed abstract class GithubCheckRunConclusion(val value: String) extends StringEnumEntry
case object GithubCheckRunConclusion extends StringEnum[GithubCheckRunConclusion] with StringCirceEnum[GithubCheckRunConclusion] {
  def values = findValues

  case object Success        extends GithubCheckRunConclusion("success")
  case object Failure        extends GithubCheckRunConclusion("failure")
  case object Neutral        extends GithubCheckRunConclusion("neutral")
  case object Cancelled      extends GithubCheckRunConclusion("cancelled")
  case object TimedOut       extends GithubCheckRunConclusion("timed_out")
  case object ActionRequired extends GithubCheckRunConclusion("action_required")
  case object Stale          extends GithubCheckRunConclusion("stale")
}
