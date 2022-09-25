import akka.kafka.{ProducerMessage, ProducerSettings}
import akka.kafka.ProducerMessage.MultiResultPart
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl._
import com.typesafe.config.ConfigFactory
import io.prometheus.client.exporter.HTTPServer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.util.Random

object Main extends App {

  import io.prometheus.client.Counter
  val messagesPublished: Counter =
    Counter.build
      .name("messages_published")
      .help("Total messages published to Kafka.")
      .register

  val server: HTTPServer = new HTTPServer.Builder()
    .withPort(9095)
    .build()

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

  val bootstrapServers =
    config.getString("akka.kafka.producer.kafka-clients.bootstrap.servers")
  val topic = config.getString("topic")

  val producerSettings =
    ProducerSettings(system, new StringSerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServers)

  object amount {
    val messages = config.getInt("messagesAmount")
    val users = messages / 10
    val chats = messages / 1000
  }

  println(s"""
      |
      |  BENCHMARK
      |  
      |  - messages: ${amount.messages}
      |  - topic: ${topic}
      |  
      |  
      |""".stripMargin)
  object dataset {
    val messages = Seq(
      "hello!",
      "hi"
    )
  }

  case class UserId(id: String)
  case class ChatId(id: String)
  case class Message(from: UserId, text: String, to: ChatId)

  object Message {
    val greetings = Seq(
      "hello ",
      "nice to meet you ",
      "so long time ",
      "Hello Mr. ",
      "Do you want to buy a boat? "
    )
    val N = greetings.size
    val possibleMessagesN: Int => UserId => String = { index => userId =>
      greetings(index % N) + userId.id
    }

    def generator(index: Int) = {
      val userId = UserId(s"user-${index % amount.users}")
      val chatId = ChatId(s"chat-${index % amount.chats}")
      Message(
        from = userId,
        text = possibleMessagesN(index)(userId),
        to = chatId
      )
    }
  }

  val done = Source(1 to amount.messages)
    .map { i =>
      val message = Message.generator(i)
      messagesPublished.inc()
      ProducerMessage.single(
        new ProducerRecord(
          topic,
          message.to.id,
          s"${message.from.id}: ${message.text}"
        ),
        i
      )
    }
    .via(Producer.flexiFlow(producerSettings))
    .runWith(Sink.ignore)
}
