package example.complex

import com.devsisters.shardcake._
import com.devsisters.shardcake.interfaces.Serialization
import dev.profunktor.redis4cats.RedisCommands
import example.complex.GuildBehavior.GuildMessage.Join
import example.complex.GuildBehavior._
import zio._

object GuildApp extends ZIOAppDefault {
  val config: ZLayer[Any, SecurityException, Config] =
    ZLayer(
      System
        .env("port")
        .map(_.flatMap(_.toIntOption).fold(Config.default)(port => Config.default.copy(shardingPort = port)))
    )

  val program: ZIO[Sharding with Scope with Serialization with RedisCommands[Task, String, String], Throwable, Unit] =
    for {
      _     <- Sharding.registerEntity(Guild, behavior)
      _     <- Sharding.registerScoped
      guild <- Sharding.messenger(Guild)
      user1 <- Random.nextUUID.map(_.toString)
      user2 <- Random.nextUUID.map(_.toString)
      user3 <- Random.nextUUID.map(_.toString)
      _     <- guild.send("guild1")(Join(user1, _)).debug
      _     <- guild.send("guild1")(Join(user2, _)).debug
      _     <- guild.send("guild1")(Join(user3, _)).debug
      // TODO WHY THIS CODE CANNOT BE WRITTEN? Error: dead code following this construct
      // REFERENCE https://github.com/devsisters/shardcake/blob/c11df6ec95374a168c8e44e94ee21de4220849ec/examples/src/main/scala/example/complex/GuildApp.scala#L29
      //  _     <- ZIO.never
    } yield ()

  def run: Task[Unit] =
    ZIO
      .scoped(program)
      .provide(
        config,
        ZLayer.succeed(GrpcConfig.default),
        ZLayer.succeed(RedisConfig.default),
        redis,
        StorageRedis.live,
        KryoSerialization.live,
        ShardManagerClient.liveWithSttp,
        GrpcPods.live,
        Sharding.live,
        GrpcShardingService.live
      )
}
