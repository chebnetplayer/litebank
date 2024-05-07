package misis

import akka.actor.ActorSystem
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.scaladsl.{Flow, Source}
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.reflect.ClassTag

trait WithKafka {

    implicit def system: ActorSystem
    def group: String

    val consumerConfig = system.settings.config.getConfig("akka.kafka.consumer")
    val consumerSettings = ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer)
        .withProperty(ConsumerConfig.GROUP_ID_CONFIG, group)
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val producerConfig = system.settings.config.getConfig("akka.kafka.producer")
    val producerSettings = ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)

    implicit def simpleTopicName[T](implicit tag: ClassTag[T]): TopicName[T] = () => tag.runtimeClass.getSimpleName

    def kafkaSource[T](implicit decoder: Decoder[T], topicName: TopicName[T]) = Consumer
        .committableSource(consumerSettings, Subscriptions.topics(topicName.get))
        .map(message => message.record.value())
        .map(body => decode[T](body))
        .collect {
            case Right(command) =>
                command
            case Left(error) => throw new RuntimeException(s"Ошибка при разборе сообщения $error")
        }
        .log(s"Случилась ошибка при чтении топика ${topicName.get}")

    def kafkaSink[T](implicit encoder: Encoder[T], topicName: TopicName[T]) = Flow[T]
        .map(event => event.asJson.noSpaces)
        .map(value => new ProducerRecord[String, String](topicName.get, value))
        .log(s"Случилась ошибка при обработке сообщения из топика ${topicName.get}")
        .to(Producer.plainSink(producerSettings))

    def produceCommand[T](command: T)(implicit encoder: Encoder[T], topicName: TopicName[T]) = {
        Source
            .single(command)
            .map { command =>
                println(s"Отправляется  сообщение $command в топик ${topicName.get}")
                command.asJson.noSpaces
            }
            .map { value =>
                new ProducerRecord[String, String](topicName.get, value)
            }
            .to(Producer.plainSink(producerSettings))
            .run()
    }
}

trait TopicName[T] {
    def get(): String
}
