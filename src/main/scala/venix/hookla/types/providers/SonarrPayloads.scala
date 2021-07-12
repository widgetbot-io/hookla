package venix.hookla.types.providers

import cats.syntax.functor._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import venix.hookla.types.BasePayload

sealed trait SonarrPayload extends BasePayload

case class Series(
    id: Int,
    title: String,
    path: String,
    tvdbId: Option[Int]
)

case class Release(
    quality: Option[String],
    qualityVersion: Option[Int],
    releaseGroup: Option[String],
    releaseTitle: Option[String],
    indexer: Option[String],
    size: Option[Int]
)

case class Episode(
    id: Int,
    episodeNumber: Int,
    seasonNumber: Int,
    title: String
//  airDate: Option[LocalDateTime],
//  airDateUtc: Option[LocalDateTime]
)

case class EpisodeFile(
    id: Int,
    relativePath: String,
    path: String,
    quality: Option[String],
    qualityVersion: Option[Int],
    releaseGroup: Option[String],
    sceneName: Option[String]
)

case class SonarrGrabEvent(
    eventType: SonarrEventType,
    series: Series,
    episodes: List[Episode],
    release: Release,
    episodeFile: Option[EpisodeFile],
    isUpgrade: Option[Boolean]
) extends SonarrPayload

case class SonarrDownloadEvent(
    eventType: SonarrEventType,
    series: Series,
    episodes: List[Episode],
    release: Option[Release],
    episodeFile: EpisodeFile,
    isUpgrade: Boolean
) extends SonarrPayload

case class SonarrRenameEvent(
    eventType: SonarrEventType,
    series: Series,
    episodes: Option[List[Episode]],
    release: Option[Release],
    episodeFile: Option[EpisodeFile],
    isUpgrade: Option[Boolean]
) extends SonarrPayload

case class SonarrTestEvent(
    eventType: SonarrEventType,
    series: Series,
    episodes: Option[List[Episode]],
    release: Option[Release],
    episodeFile: Option[EpisodeFile],
    isUpgrade: Option[Boolean]
) extends SonarrPayload

object SonarrPayloads {

  val sonarrEvents: Map[String, Decoder[SonarrPayload]] = Map(
    SonarrEventType.Grab.toString -> Decoder[SonarrGrabEvent].widen,
    SonarrEventType.Test.toString -> Decoder[SonarrTestEvent].widen
  )

}
