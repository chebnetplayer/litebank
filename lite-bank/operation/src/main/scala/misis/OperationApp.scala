package misis

import akka.actor.ActorSystem
import scala.concurrent.Future
import scala.util.Success
import scala.concurrent.ExecutionContext
import slick.jdbc.PostgresProfile.api._
import misis.route._
import scala.io.StdIn
import akka.http.scaladsl.Http
import misis.repository.Repository
import misis.kafka.Streams
import misis.model._
import misis.kafka.TopicName
import akka.http.scaladsl.server.Directives._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

object OperationApp extends App  {
    implicit val system: ActorSystem = ActorSystem("App")
    implicit val ec = system.dispatcher
    implicit val db = Database.forConfig("database.postgres")

    val route = new Route()

    private val streams = new Streams()
    private val repository = new Repository(streams)
    val mainRoute = new MainRoute(streams, repository)
    implicit val commandTopicName: TopicName[BankAccountCreate] = streams.simpleTopicName[BankAccountCreate]
    streams.produceCommand(BankAccountCreate(0))

    val bindFuture =
        Http().newServerAt("0.0.0.0", 8081).bind(route.routes ~ mainRoute.routes)
}