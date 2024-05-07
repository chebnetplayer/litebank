package misis.route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import misis.kafka.TopicName
import misis.kafka.Streams
import misis.model.{AccountUpdate}
import misis.model._
import misis.repository.Repository
import scala.concurrent.ExecutionContext


class MainRoute(streams: Streams, repository: Repository)(implicit ec: ExecutionContext) extends FailFastCirceSupport {

    implicit val commandTopicName: TopicName[AccountUpdate] = streams.simpleTopicName[AccountUpdate]

    def routes =
        (path("hello") & get) {
            complete("ok")
        } ~
          (path("update" / IntNumber / IntNumber) { (accountId, value) =>
            val command = AccountUpdate(accountId, value)
            streams.produceCommand(command)
            complete(command)
          }) ~
          (path("transfer") & post & entity(as[TransferStart])) { transfer =>
            repository.startTransfer(transfer)
            complete(transfer)
          } ~
          (path("account") & post & entity(as[AccountCreate])) { createAccount =>
            repository.createAccount(createAccount)
            complete(createAccount)
          } ~
          (path("account" / IntNumber) & get) { accountId =>
            val command = ShowAccountBalance(accountId)
            repository.showAccountBalance(command)
            complete(command)
          } ~
          (path("cashback" / IntNumber) & post) { accountId =>
            val command = ReturnCashback(accountId)
            repository.returnCashback(command)
            complete(command)
          }
}


