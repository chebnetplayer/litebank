package misis.kafka

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import io.circe.generic.auto._
import misis.WithKafka
import misis.model.AccountUpdated
import misis.repository.ElasticRepository

import scala.concurrent.ExecutionContext

class ReportStreams(repository: ElasticRepository)(implicit val system: ActorSystem, executionContext: ExecutionContext)
    extends WithKafka {
    override def group: String = "report"

    kafkaSource[AccountUpdated]
        .mapAsync (1){repository.index}
        .to(Sink.ignore)
        .run()
}
