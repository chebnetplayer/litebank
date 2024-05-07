package misis.kafka

import io.circe._
import io.circe.parser._
import io.circe.syntax._
import io.circe.generic.auto._
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import misis.kafka.WithKafka
import misis.model._

import scala.concurrent.ExecutionContext

class Streams()(implicit val system: ActorSystem, executionContext: ExecutionContext) extends WithKafka {
    override def group: String = "operation"

    kafkaSource[AccountFromAck]
      .map { e =>
          println(
              s"Получено TransferFromAccount и отправлено TransferCheckDestination. Запрос на наличие учетной записи ${e.sourceId}"
          )
          TransferCheckDestination(e.sourceId, e.destinationId, e.value)
      }
      .to(kafkaSink)
      .run()

    kafkaSource[AccountUpdated]
      .filter(event => event.nextAccountId.isDefined)
      .map { e =>
          println(s"Отправить запрос на начисление")
          AccountUpdate(e.nextAccountId.getOrElse(0), -e.value, 0, None, Some(e.accountId))
      }
      .to(kafkaSink)
      .run()
}