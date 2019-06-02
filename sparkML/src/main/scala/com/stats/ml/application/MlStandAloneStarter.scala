package com.stats.ml.application

import io.vertx.scala.core.Vertx

object MlStandAloneStarter {
  def main(args: Array[String]): Unit = {
    println("Access ML Serive @ http://localhost:8009/ ")
    println("About to deploy scala ML verticles...")
    val vertx = Vertx.vertx()
    vertx.deployVerticle("scala:com.stats.ml.application.MlVerticleStarter")
    vertx.deployVerticle("scala:com.stats.ml.application.PeriodicMlAppPoolVerticle")
    vertx.deployVerticle("scala:com.stats.ml.application.PeriodicKafkaConsumer")
    println("Deployed scala verticles successfully.....")
  }
} 