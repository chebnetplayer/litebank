package misis.repository

import com.sksamuel.elastic4s.{ElasticClient, Response}
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.fields._
import com.sksamuel.elastic4s.requests.indexes.IndexResponse
import io.circe.Json
import misis.model.AccountUpdated
import io.circe.generic._
import io.circe.syntax._
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}

class ElasticRepository(elastic: ElasticClient)(implicit ec: ExecutionContext) {

    val indexName = "account-updated"

    val mapping = createIndex(indexName)
        .mapping(properties(
            KeywordField("operationId"),
            IntegerField("accountId"),
            IntegerField("value"),
            DateField("publishedAt"),
            TextField("category"),
            TextField("tags")
        ))

    elastic
        .execute(indexExists(indexName))
        .filter(res => res.isSuccess && !res.result.isExists)
        .flatMap(_ => elastic.execute(mapping))
        .map(Some(_))
        .recover { case e: NoSuchElementException => None }

    def index(event: AccountUpdated): Future[Response[IndexResponse]] = {
        val json = event.asJson.noSpaces
        elastic
            .execute {
                indexInto(indexName)
                    .doc(json)
            }
            .map { res =>
                println(res)
                res
            }
    }
}
