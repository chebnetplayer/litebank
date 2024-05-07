package misis

import akka.actor.ActorSystem
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties}

object RandomApp extends App  {
    implicit val system: ActorSystem = ActorSystem("MyApp")
    implicit val ec = system.dispatcher
    val elastic = ElasticClient(JavaClient(ElasticProperties("http://localhost:9200")))
    new InitAccount(elastic)
}
