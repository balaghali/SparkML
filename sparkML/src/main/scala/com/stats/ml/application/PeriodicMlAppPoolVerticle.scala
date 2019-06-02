package com.stats.ml.application
import java.util.concurrent.atomic.AtomicBoolean

import io.vertx.core.logging.LoggerFactory
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.eventbus.Message
import scala.util.Failure
import scala.util.Success
import io.vertx.scala.core.Vertx
import io.vertx.scala.core.Future

class PeriodicMlAppPoolVerticle extends ScalaVerticle {

  val logger = LoggerFactory.getLogger("PeriodicMlAppPoolVerticle")
  val isEnabled = new AtomicBoolean(false)
  override def start(): Unit = {
    println("Starting PeriodicMlAppPoolVerticle")
    
    vertx.eventBus().consumer("events-config", { message: Message[String] =>

      val msgBody: String = message.body().asInstanceOf[String]
      if (msgBody.contains("StartPublish")) {
        println("Collecting data for machine learning")
        isEnabled.set(true)
      }
      if (msgBody.contains("StopPublish")) {
        println("Stopeed collecting data for machine learning")
        isEnabled.set(false)
      }
    })
    
    vertx.setPeriodic(1200, (id: Long) => {
      println("timer fired")

      if (!isEnabled.get()) {
        vertx.eventBus().publish("events-feed", "Not Running ML algorithm - MLProcessor")
      }else {
        vertx.eventBus().publish("events-feed", "Go to /templateDef to view the status of MLProcessor")
        vertx.executeBlocking(() => MLProcessor.process(), false)
        .onComplete{  // <2>
          case Success(s) => vertx.eventBus().publish("template-feed", "ML algorithm model inference rmse is :"+s) // <3>
          case Failure(t) => t.printStackTrace()
        }
      }        
    })
  }

  override def stop(): Unit = {
    println("Stopping PeriodicMlAppPoolingVerticleStarter")
  }
}