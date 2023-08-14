package venix.hookla.services.core

import io.circe
import io.circe.{Decoder, Encoder}
import sttp.client3._
import sttp.client3.circe._
import sttp.client3.httpclient.zio.SttpClient
import sttp.model.{Header, Method, StatusCode, Uri}
import venix.hookla.RequestError._
import venix.hookla.resolvers.{ISchemaResolver, ISinkResolver, ISourceResolver, IUserResolver, SchemaResolver}
import zio._

case class Options(
    headers: Map[String, String] = Map.empty,
    baseRequest: RequestT[Empty, Either[String, String], Any] = basicRequest,
    requestOptions: Option[RequestOptions] = None
) {
  def addHeader(k: String, v: String): Options = copy(headers + (k -> v))
}

trait IHTTPService {
  def get[R](uri: Uri, options: Options = Options())(implicit decoder: Decoder[R]): Result[R]
  def post[B, R](uri: Uri, body: B, options: Options = Options())(implicit encoder: Encoder[B], decoder: Decoder[R]): Result[R]
  // TODO: Create DELETE, PATCH etc.
}

class HTTPService(
    private val sttpBackend: SttpClient
) extends IHTTPService {
  private def toRequest[B](request: RequestT[Empty, Either[String, String], Any], method: Method, uri: Uri): Request[Either[String, String], Any] =
    request.method(method, uri)

  private def constructRequest(req: RequestT[Empty, Either[String, String], Any], options: Options = Options()): RequestT[Empty, Either[String, String], Any] =
    req.copy(
      headers = options.headers.map(h => Header(h._1, h._2)).toSeq ++ req.headers,
      options = options.requestOptions.getOrElse(req.options)
    )

  def get[R](uri: Uri, options: Options = Options())(implicit decoder: Decoder[R]): Result[R] =
    sendRequest {
      toRequest(constructRequest(options.baseRequest, options), Method.GET, uri).response(asJson[R])
    }

  def post[B, R](uri: Uri, body: B, options: Options = Options())(implicit encoder: Encoder[B], decoder: Decoder[R]): Result[R] =
    sendRequest {
      toRequest(
        constructRequest(options.baseRequest, options),
        Method.GET,
        uri
      )
        .body(body)
        .response(asJson[R])
    }

  private def sendRequest[R](req: Request[Either[ResponseException[String, circe.Error], R], Any]): Result[R] =
    sttpBackend
      .send(req)
      .disconnect
      .timeoutFail(RequestTimeout(s"Request timeout: $req"))(new DurationSyntax(5).seconds)
      .reject {
        case r if r.code == StatusCode.InternalServerError => InternalServerError(r.toString())
        case r if r.code == StatusCode.ServiceUnavailable  => ServiceUnavailable(r.toString())
        case r if r.code == StatusCode.GatewayTimeout      => GatewayTimeout(r.toString())
        case r if r.code == StatusCode.TooManyRequests     => TooManyRequests(r.toString())
        case r if r.code == StatusCode.Unauthorized =>
          println(r)
          Unauthorized(r.toString())
        case r if r.code == StatusCode.BadRequest =>
          println(r)
          BadRequest(r.toString())
        case r if r.code != StatusCode.Ok =>
          println(r)
          GenericHttpError(r.toString())
      }
      .map(_.body)
      .absolve
      .mapError {
        case e: HTTPError => e
        case e: io.circe.Error =>
          println(e.fillInStackTrace())
          DecodingError(e)
        case e: DeserializationException[_] =>
          println("DeserializationException")
          println(e.getMessage)
          println(e.body)
          DeserializationError(e.getMessage)
        case e: Throwable =>
          println(e.fillInStackTrace())
          GenericHttpError(e.getMessage)
      }
}

object HTTPService {
  private type In = SttpClient
  private def create(client: SttpClient) = new HTTPService(client)

  val live: ZLayer[In, Throwable, IHTTPService] = ZLayer.fromFunction(create _)
}
