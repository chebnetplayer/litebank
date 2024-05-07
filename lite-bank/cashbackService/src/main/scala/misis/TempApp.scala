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

    private val repository = new Repository()
    private val streams = new Streams(repository)

    val bindFuture =
        Http().newServerAt("0.0.0.0", 8081).bind(route.routes)
}

