package misis

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import misis.kafka.AccountStreams
import misis.repository.AccountRepository
import misis.route.AccountRoute

object AccountApp extends App  {
    implicit val system: ActorSystem = ActorSystem("App")
    implicit val ec = system.dispatcher

    val groupId = ConfigFactory.load().getInt("account.id")

    val route = new AccountRoute()

    private val repository = new AccountRepository()
    private val streams = new AccountStreams(repository, groupId)

    val bindFuture =
    Http().newServerAt("0.0.0.0", 8081).bind(route.routes)
}
