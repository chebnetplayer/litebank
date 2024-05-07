package misis

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import misis.kafka.{Streams}
import misis.repository.{Repository}
import misis.route.Route

object TempApp extends App  {
    implicit val system: ActorSystem = ActorSystem("App")
    implicit val ec = system.dispatcher

    val route = new Route()

    val defaultFeeLimit = 100
    val defaultFeePercent = 8

    private val repository = new Repository(defaultFeeLimit)
    private val streams = new Streams(repository, defaultFeePercent)

    val bindFuture =
        Http().newServerAt("0.0.0.0", 8081).bind(route.routes)
}