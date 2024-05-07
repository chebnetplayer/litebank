package misis.kafka

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Sink, Source}
import io.circe.generic.auto._
import io.circe.syntax._
import misis.WithKafka
import misis.model.{AccountUpdate, AccountUpdated}
import misis.repository.Repository
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.ExecutionContext

class Streams(repository: Repository)(implicit val system: ActorSystem, executionContext: ExecutionContext)
    extends WithKafka {
//    override def group: String = ???
}
