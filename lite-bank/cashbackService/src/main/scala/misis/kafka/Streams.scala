package misis.kafka

import io.circe._
import io.circe.parser._
import io.circe.syntax._
import io.circe.generic.auto._
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import misis.kafka.WithKafka
import misis.repository._
import misis.model._

import scala.concurrent.ExecutionContext

class Streams(repository: Repository)(implicit val system: ActorSystem, executionContext: ExecutionContext)
    extends WithKafka {
  override def group: String = "cashback"

  def bankAccountId: Int = 0

  kafkaSource[AccountUpdated]
    .filter(event => event.previousAccountId.isDefined && event.accountId != bankAccountId)
    .map { event =>
      val cashbackAccountId = event.previousAccountId.getOrElse(-1)
      val cashbackAmount =
        repository
          .calculateCashbackAmount(event.category.getOrElse(-1), event.value)
          .getOrElse(0)
      println(s"Добавить кешбэк аккаунту ${cashbackAccountId}")
      if (!repository.isCashbackOpen(cashbackAccountId)) {
        println(s"Кешбэк для аккаунта ${cashbackAccountId} не найден")
        repository.createCashback(cashbackAccountId, cashbackAmount)
      } else {
        println(s"Кешбэк для аккаунта ${cashbackAccountId} найден")
        repository.updateCashback(cashbackAccountId, cashbackAmount)
      }
      CashbackUpdated(cashbackAccountId, cashbackAmount)
    }
    .to(kafkaSink)
    .run()

  kafkaSource[CashbackUpdated]
    .mapAsync(1) { event =>
      repository.getCashback(event.accountId).map {
        case Some(accountCashback) =>
          println(
            s"Обновлён кешбэк для аккаунта ${event.accountId} на сумму ${event.value}. Текущий кешбэк: ${accountCashback.cashback}"
          )
          event
        case None =>
          println(
            s"Проблемы с аккаунтом ${event.accountId}"
          )
          event
      }
    }
    .to(Sink.ignore)
    .run()

  kafkaSource[ReturnCashback]
    .mapAsync(1) { command =>
      repository.getCashback(command.accountId).map {
        case Some(accountCashback) =>
          println(
            s"Кешбэк должен вернуться на счёт ${command.accountId}. Текущий кешбэк: ${accountCashback.cashback}"
          )
          repository.resetCashback(command.accountId)
          AccountUpdate(command.accountId, accountCashback.cashback, 0, None, None)
        case None =>
          AccountUpdate(-1, 0, 0, None, None)
      }
    }
    .to(kafkaSink)
    .run()
}
