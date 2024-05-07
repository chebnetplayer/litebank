package misis

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import misis.kafka.Streams
import misis.model.AccountUpdate
import misis.repository.Repository
import misis.route.Route
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._


object TempApp extends App  {
    implicit val system: ActorSystem = ActorSystem("MyApp")
    implicit val ec = system.dispatcher
    val port = ConfigFactory.load().getInt("port")

    private val repository = new Repository()
    private val streams = new Streams(repository)

    private val route = new Route()
    Http().newServerAt("0.0.0.0", port).bind(route.routes)
}
