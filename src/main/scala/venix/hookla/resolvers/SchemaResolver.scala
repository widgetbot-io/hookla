package venix.hookla.resolvers

import caliban.CalibanError.ExecutionError
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers.{printErrors, timeout}
import caliban._
import caliban.schema._
import venix.hookla.Args.CreateTeamArgs
import venix.hookla.RequestError.{DatabaseError, InvalidRequest, InvalidRequestPayload, UnknownError}
import venix.hookla.{CustomSchema, Env, Mutations, Queries, Result}
import zio.{UIO, URIO, ZIO, ZLayer, durationInt}
import venix.hookla.entities._
import venix.hookla.types._

import java.util.UUID
import scala.language.postfixOps

trait ISchemaResolver {
  def graphQL: GraphQL[Env]
}

class SchemaResolver(
    private val teamResolver: ITeamResolver,
    private val userResolver: IUserResolver,
    private val sourceResolver: ISourceResolver,
    private val sinkResolver: ISinkResolver,
    private val hookResolver: IHookResolver
) extends ISchemaResolver
    with GenericSchema[Env] {

  implicit def customEffectSchema[A: CustomSchema]: CustomSchema[Result[A]] =
    Schema.customErrorEffectSchema {
      // TODO: Change in production
      case DatabaseError(cause)           => ExecutionError(cause.getMessage)
      case InvalidRequest(message)        => ExecutionError(message)
      case InvalidRequestPayload(message) => ExecutionError(message)
      case UnknownError                   => ExecutionError("Something went wrong, please try again later.")
      case _                              => ExecutionError("Something went wrong, please don't try again later.")
    }

  // Arguments
  import venix.hookla.Args._
  implicit val createTeamArgs: ArgBuilder[CreateTeamArgs]                     = ArgBuilder.gen[CreateTeamArgs]
  implicit val createTeamArgsSchema: CustomSchema[CreateTeamArgs]             = gen[Env, CreateTeamArgs]
  implicit val updateTeamArgs: ArgBuilder[UpdateTeamArgs]                     = ArgBuilder.gen[UpdateTeamArgs]
  implicit val updateTeamArgsSchema: CustomSchema[UpdateTeamArgs]             = gen[Env, UpdateTeamArgs]
  implicit val deleteTeamArgs: ArgBuilder[DeleteTeamArgs]                     = ArgBuilder.gen[DeleteTeamArgs]
  implicit val deleteTeamArgsSchema: CustomSchema[DeleteTeamArgs]             = gen[Env, DeleteTeamArgs]
  implicit val addTeamMemberArgs: ArgBuilder[AddTeamMemberArgs]               = ArgBuilder.gen[AddTeamMemberArgs]
  implicit val addTeamMemberArgsSchema: CustomSchema[AddTeamMemberArgs]       = gen[Env, AddTeamMemberArgs]
  implicit val updateTeamMemberArgs: ArgBuilder[UpdateTeamMemberArgs]         = ArgBuilder.gen[UpdateTeamMemberArgs]
  implicit val updateTeamMemberArgsSchema: CustomSchema[UpdateTeamMemberArgs] = gen[Env, UpdateTeamMemberArgs]
  implicit val removeTeamMemberArgs: ArgBuilder[RemoveTeamMemberArgs]         = ArgBuilder.gen[RemoveTeamMemberArgs]
  implicit val removeTeamMemberArgsSchema: CustomSchema[RemoveTeamMemberArgs] = gen[Env, RemoveTeamMemberArgs]

  implicit lazy val userSchema: CustomSchema[User] = obj("User", None) { implicit ft =>
    List(
      field("id")(_.id),
      field("discord")(u => userResolver.resolveDiscordUser(u.discordId)),
      field("teams")(u => userResolver.resolveTeams(UserId(u.id)))
    )
  }
  implicit lazy val teamSchema: CustomSchema[Team] = obj("Team", None) { implicit ft =>
    List(
      field("id")(_.id),
      field("name")(_.name),
      field("members")(t => teamResolver.resolveMembers(TeamId(t.id))),
      field("hooks")(t => teamResolver.resolveHooks(TeamId(t.id)))
    )
  }
  implicit lazy val hookSchema: CustomSchema[Hook] = obj("Hook", None) { implicit ft =>
    List(
      field("id")(_.id),
      field("source")(h => Source("id", "name", "icon")),
      field("team")(h => hookResolver.resolveTeam(h)),
      field("sinks")(h => hookResolver.resolveSinks(h))
    )
  }
  implicit lazy val hookSinkSchema: CustomSchema[HookSink] = obj("HookSink", None) { implicit ft =>
    List(
      field("id")(_.id),
      field("sink")(h => Sink("id", "name", "icon")),
      field("hook")(h => sinkResolver.resolveHook(h))
    )
  }

  implicit lazy val sourceSchema: CustomSchema[Source]           = gen
  implicit lazy val sinkSchema: CustomSchema[Sink]               = gen
  implicit lazy val discordUserSchema: CustomSchema[DiscordUser] = gen
  implicit lazy val queriesSchema: CustomSchema[Queries]         = gen
  implicit lazy val mutationsSchema: CustomSchema[Mutations]     = gen

  def graphQL: GraphQL[Env] = caliban.graphQL[Env, Queries, Mutations, Unit](rootResolver) @@ printErrors @@ timeout(3 seconds) @@ apolloTracing

  private def rootResolver: RootResolver[Queries, Mutations, Unit] =
    RootResolver(
      Queries(
        sinks = sinkResolver.getAll,
        sources = sourceResolver.getAll,
        me = userResolver.me,
        teams = teamResolver.getForMe
      ),
      Mutations(
        createTeam = args => teamResolver.create(args.name),
        updateTeam = args => teamResolver.update(TeamId(args.id), args.name),
        deleteTeam = args => teamResolver.delete(TeamId(args.id)),
        addTeamMember = args => teamResolver.addMember(TeamId(args.teamId), UserId(args.userId)),
        updateTeamMember = args => teamResolver.updateMember(TeamId(args.teamId), UserId(args.userId), args.admin),
        removeTeamMember = args => teamResolver.removeMember(TeamId(args.teamId), UserId(args.userId))
      )
    )
}

object SchemaResolver {
  private type In = ISourceResolver with ISinkResolver with IUserResolver with ITeamResolver with IHookResolver
  private def create(sourceResolver: ISourceResolver, sinkResolver: ISinkResolver, userResolver: IUserResolver, d: ITeamResolver, e: IHookResolver) = new SchemaResolver(d, userResolver, sourceResolver, sinkResolver, e)

  val live: ZLayer[In, Throwable, ISchemaResolver] = ZLayer.fromFunction(create _)
}
