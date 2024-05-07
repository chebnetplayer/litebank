package misis.kafka

import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.scaladsl.{Flow, Sink, Source}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer, StringSerializer}
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import io.circe.generic.auto._
import misis.model._
import scala.reflect.ClassTag
import scala.util.{Failure, Success}
import com.typesafe.config.ConfigFactory
import scala.concurrent.Future
import misis.repository._

class AccountStreams(repository: AccountRepository, groupId: Int)(implicit val system: ActorSystem, executionContext: ExecutionContext)
    extends WithKafka {

    def group = s"account-${groupId}"

  kafkaSource[AccountCreate]
    .mapAsync(1) { command =>
      println(s"${groupId} попытка создать аккаунт с суммой: ${command.amount}")
      repository
        .createAccount(command.amount, command.category)
        .map(account => AccountCreated(account.id, account.amount))
    }
    .to(kafkaSink)
    .run()

  kafkaSource[BankAccountCreate]
    .mapAsync(1) { command =>
      repository
        .createBankAccount(command.amount)
        .map(account => AccountCreated(account.id, account.amount))
    }
    .to(kafkaSink)
    .run()

  kafkaSource[ShowAccountBalance]
    .mapAsync(1) { command =>
      repository.getAccount(command.accountId).map {
        case Some(account) =>
          println(s"Аккаунт ${command.accountId} имеет баланс: ${account.amount}")
          command
        case None =>
          println(
            s"Аккаунт ${command.accountId} не найден. Доступные аккаунты: ${repository.getAccountKeys()}"
          )
          command
      }
    }
    .to(Sink.ignore)
    .run()

  kafkaSource[TransferStart]
    .map(command => {
      println("Получено сообщение о TransferStart")
      command
    })
    .filter(command => repository.isAccountOpen(command.sourceId))
    .map { command =>
      println(s"Отправлено AccountFromAck. Аккаунт ${command.sourceId} открыт и имеет достатосное количество денег на счету")
      AccountFromAck(command.sourceId, command.destinationId, command.value)
    }
    .to(kafkaSink)
    .run()

  kafkaSource[TransferCheckDestination]
    .map(command => {
      println("Сообщение о TransferCheckDestination")
      command
    })
    .filter(command => {
      println(
        s"repository.accountExists(command.destinationId) ${repository.isAccountOpen(command.destinationId)}"
      )
      repository.isAccountOpen(command.destinationId)
    })
    .map { command =>
      println(s"Отправлено AccountToAck. Аккаунт ${command.destinationId} открыт")
      AccountToAck(command.sourceId, command.destinationId, command.value)
    }
    .to(kafkaSink)
    .run()

  kafkaSource[AccountUpdate]
    .map(command => {
      println(s"Необходимо обнваить аккаунт ${command.accountId} с суммой ${command.value}")
      println(s"Аккаунт ${command.accountId} открыт: ${repository.isAccountOpen(command.accountId)}")
      println(s"Размер комиссии:  ${command.feeValue}")
      command
    })
    .mapAsync(1) { command =>
      val category = repository.getAccountCategory(command.accountId).getOrElse(0)
      repository.updateAccount(command.accountId, command.value - command.feeValue)
        .map(_ =>
          AccountUpdated(
            command.accountId,
            command.value,
            command.feeValue,
            command.nextAccountId,
            command.previousAccountId,
            Some(category)
          )
        )
    }
    .to(kafkaSink)
    .run()

  kafkaSource[AccountCreated]
    .filter(event => repository.isAccountOpen(event.accountId))
    .map {event =>
      println(s"[Группа ${groupId}]. Аккаунт ${event.accountId} был создан с суммой: ${event.amount}")
      event
    }
    .to(Sink.ignore)
    .run()

  kafkaSource[AccountUpdated]
    .filter(event => repository.isAccountOpen(event.accountId))
    .mapAsync(1) { event =>
      repository.getAccount(event.accountId).map {
        case Some(account) =>
          if (event.accountId == 0) {
            println(
              s"Аккаунт банка был изменён на ${event.value - event.feeValue}. Текущий баланс счёта банка: ${account.amount}"
            )
          } else {
            println(s"Аккаунт ${event.accountId} был обновлён на сумму ${event.value}. Текущий баланс: ${account.amount}")
          }
          event
        case None => event
      }
    }
    .to(Sink.ignore)
    .run()
}
