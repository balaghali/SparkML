package com.stats.ml.application

import java.util.Collection
import java.util.Properties

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import io.vertx.lang.scala.ScalaVerticle
import java.util.ArrayList

class PeriodicKafkaConsumer extends ScalaVerticle {

  override def start(): Unit = {
    println("starting PeriodicKafkaConsumer")
    val properties = new Properties()
    properties.put("bootstrap.servers", "localhost:9092")
    properties.put("group.id", "consumer-ml")
    properties.put("key.deserializer", classOf[StringDeserializer])
    properties.put("value.deserializer", classOf[StringDeserializer])
    properties.put("value.deserializer", classOf[StringDeserializer])
    properties.put("auto.offset.reset", "latest")
    properties.put("enable.auto.commit", (true: java.lang.Boolean))

    val collection: Collection[String] = new ArrayList[String]()
    collection.add("kafkamltopic")
    collection.add("mlScalaTopic")
//    "kafkamltopic" , "mlScalaTopic"
    val kafkaConsumer = new KafkaConsumer[String, String](properties)
    kafkaConsumer.subscribe(collection)

    vertx.setPeriodic(1200, (id: Long) => {
      val records = kafkaConsumer.poll(500)
      records.forEach(record => {
        vertx.eventBus().publish("template-feed", record.value)
      })
    })
  }
}