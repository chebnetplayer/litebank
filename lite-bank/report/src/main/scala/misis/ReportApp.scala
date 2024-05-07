package misis

import akka.actor.ActorSystem
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties}
import misis.kafka.ReportStreams
import misis.repository.ElasticRepository

object ReportApp extends App  {
    implicit val system: ActorSystem = ActorSystem("MyApp")
    implicit val ec = system.dispatcher
    val elastic = ElasticClient(JavaClient(ElasticProperties("http://localhost:9200")))

    private val repository = new ElasticRepository(elastic)
    private val streams = new ReportStreams(repository)
}
