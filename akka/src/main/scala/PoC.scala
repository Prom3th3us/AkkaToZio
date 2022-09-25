import Main.producerSettings
import akka.{Done, NotUsed}
import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.sharding._
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.pattern.{ask, pipe}
import akka.stream.{OverflowStrategy, QueueOfferResult}
import akka.stream.scaladsl._
import akka.util.{ByteString, Timeout}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object PoC extends App {

  implicit val system = akka.actor.ActorSystem("benchmark")
  val config = ConfigFactory
    .parseString(s"""
                    |
                    |akka.cluster.seed-nodes=["akka://benchmark@0.0.0.0:2551"]
                    |akka.remote.artery.canonical.port=2551
                    |akka.management.http.port=8551
                    |akka.management.http.bind-port=8551
                    |""".stripMargin)
    .withFallback(ConfigFactory.load())

  trait HasRequestId { val requestId: RequestId }
  case class RequestId(id: String)
  case class POST(requestId: RequestId, payload: String) extends HasRequestId
  case class RESPONSE(requestId: RequestId, payload: String)
      extends HasRequestId
  trait Kafka {
    def sendMessage(post: POST): Future[QueueOfferResult]
  }
  class RequestActor(kafka: Kafka) extends Actor {
    val inflight: mutable.Map[RequestId, ActorRef] = mutable.Map.empty
    override def receive: Receive = {
      case post: POST =>
        inflight.addOne(post.requestId -> sender())
        //kafka.sendMessage(post) ma
      case response: RESPONSE =>
        inflight.remove(response.requestId).map { ref =>
          ref ! response.payload
        }
    }
  }

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case POST(id, payload)     => (id.id, payload)
    case RESPONSE(id, payload) => (id.id, payload)
  }

  val numberOfShards = 100

  val extractShardId: ShardRegion.ExtractShardId = {
    case POST(id, _)                 => (id.id.hashCode % numberOfShards).toString
    case RESPONSE(id, _)             => (id.id.hashCode % numberOfShards).toString
    case ShardRegion.StartEntity(id) =>
      // StartEntity is used by remembering entities feature
      (id.toLong % numberOfShards).toString
    case _ => throw new IllegalArgumentException()
  }

  val counterRegion: ActorRef = ClusterSharding(system).start(
    typeName = "Counter",
    entityProps = Props[RequestActor](),
    settings = ClusterShardingSettings(system),
    extractEntityId = extractEntityId,
    extractShardId = extractShardId
  )

  object KafkaMock extends Kafka {
    def pipeline: POST => Future[RESPONSE] = { post =>
      Future {
        Thread.sleep(100)
        RESPONSE(post.requestId, post.payload + " - received")
      }
    }

    override def sendMessage(post: POST): Future[QueueOfferResult] = {
      KafkaProducer.post(post)
    }

    object KafkaProducer {

      val sourceDecl =
        Source.queue[POST](bufferSize = 1000, OverflowStrategy.backpressure)
      val (sourceMat, source) = sourceDecl.preMaterialize()
      source
        .map { e =>
          new ProducerRecord[RequestId, POST]("POST", e.requestId, e)
        }
        .to(producer)
        .run()
      val post = sourceMat.offer _

    }

    consumer.map { a: ConsumerRecord[RequestId, RESPONSE] =>
      counterRegion ! a.value()
    }

    val (sink, source) =
      MergeHub.source[String].toMat(BroadcastHub.sink[String])(Keep.both).run()

    /*val framing: Flow[String, String, NotUsed] =
      Framing.delimiter(ByteString("\n"), 1024)

    val sinkWithFraming = framing.map(bytes => bytes.utf8String).to(sink)
    val sourceWithFraming = source.map(text => text + " -- ") */

    val producer: Sink[ProducerRecord[RequestId, POST], Future[Done]] =
      ??? // Producer.plainSink(producerSettings)
    val consumer
        : Source[ConsumerRecord[RequestId, RESPONSE], Consumer.Control] =
      ??? //Consumer.plainSource(???)

    val serverFlow = Flow.fromSinkAndSource(sink, source)

    serverFlow

    Tcp(system).bind("127.0.0.1", 9999).runForeach { incomingConnection =>
      incomingConnection.handleWith(???)
    }

  }

  val actor = system.actorOf(Props(new RequestActor(KafkaMock)))
  implicit val timeout = Timeout(1 second)
  for {
    response <- actor.ask(POST(RequestId("1"), "hello")).mapTo[String]
  } yield println(response)
}
