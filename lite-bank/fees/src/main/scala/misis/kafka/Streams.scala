package misis.kafka

import io.circe._
import io.circe.parser._
import io.circe.syntax._
import io.circe.generic.auto._
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import misis.kafka.WithKafka
import misis.repository.Repository
import misis.model._

import scala.concurrent.ExecutionContext

class Streams(repository: Repository, feePercent: Int)(implicit
                                                       val system: ActorSystem,
                                                       executionContext: ExecutionContext
) extends WithKafka {
  override def group: String = "fees"
  def bankAccountId: Int = 0

  kafkaSource[TransferToAccount]
    .map { e =>
      if (!repository.isAccountFeeLimitOpen(e.sourceId)) {
        repository.createFeeLimit(e.sourceId)
        println(s"Лимит аккаунта ${e.sourceId} создан")
      }
      val limit =
        repository
          .getFeeLimit(e.sourceId)
          .getOrElse(FeeLimit(e.sourceId, repository.defaultLimit))
      println(s"Аккаунт ${e.sourceId} имеет лимит: ${limit.feeLimit}")
      if (repository.isFeeLimitOver(e.sourceId, e.value)) {
        val fee = e.value * feePercent / 100
        AccountUpdate(e.sourceId, -e.value, fee, Some(e.destinationId))
      } else {
        AccountUpdate(e.sourceId, -e.value, 0, Some(e.destinationId))
      }
    }
    .to(kafkaSink)
    .run()

  kafkaSource[AccountUpdated]
    .filter(event => event.nextAccountId.isDefined)
    .map { event =>
      repository.updateFeeLimit(event.accountId, -event.value)
      println(s"Лимит аккаунта ${event.accountId} был обновлён на сумму ${event.value}")
      val limit =
        repository
          .getFeeLimit(event.accountId)
          .getOrElse(FeeLimit(event.accountId, repository.defaultLimit))
      println(s"Аккаунт ${event.accountId} имеет лимит: ${limit.feeLimit}")
      AccountUpdate(bankAccountId, event.feeValue)
    }
    .to(kafkaSink)
    .run()
}