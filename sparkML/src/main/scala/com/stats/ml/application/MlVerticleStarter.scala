package com.stats.ml.application
import scala.concurrent.Future

import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.ext.web.Router
import io.vertx.scala.ext.web.handler.StaticHandler
import io.vertx.scala.ext.web.handler.sockjs.SockJSHandler
import io.vertx.scala.ext.web.handler.sockjs.BridgeOptions
import scala.collection.mutable.Buffer
import io.vertx.scala.ext.bridge.PermittedOptions
import io.vertx.scala.core.Vertx
import io.vertx.scala.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.scala.ext.web.templ.thymeleaf.ThymeleafTemplateEngine
import java.time.LocalDateTime
import io.vertx.core.Handler
import io.vertx.scala.ext.web.RoutingContext


class MlVerticleStarter extends ScalaVerticle {

  val ROUTE_PATH = "/eventbus/*"
  val STATIC_CONTENT_PATH = "web"
  val _port = 8009
  val EVENTS_CONFIG = "events-config"
  val EVENTS_JSON = "events-json"
  val EVENTS_FEED = "events-feed"
  val TEMPLATE_DEF = "/templateDef"
  val TEMPLATE_FEED = "template-feed"
  val TEMPLATE_CONFIG = "template-config"
  val TEMPLATE_JSON = "template-json"


  override def startFuture(): Future[Unit] = { // <2>// Create a router object.
    val router: Router = Router.router(vertx)
    var inboundPermitted1 = PermittedOptions().setAddress(EVENTS_CONFIG)
    var inboundPermitted2 = PermittedOptions().setAddress(EVENTS_FEED)
    var inboundPermitted3 = PermittedOptions().setAddress(EVENTS_JSON)

    // Let through any messages coming from address 
    var outboundPermitted1 = PermittedOptions().setAddress(EVENTS_CONFIG)
    var outboundPermitted2 = PermittedOptions().setAddress(EVENTS_FEED)
    var outboundPermitted3 = PermittedOptions().setAddress(EVENTS_JSON)

     var inboundPermitted4 = PermittedOptions().setAddress(TEMPLATE_CONFIG)
    var inboundPermitted5 = PermittedOptions().setAddress(TEMPLATE_FEED)
    var inboundPermitted6 = PermittedOptions().setAddress(TEMPLATE_JSON)

    // Let through any messages coming from address 
    var outboundPermitted4 = PermittedOptions().setAddress(TEMPLATE_CONFIG)
    var outboundPermitted5 = PermittedOptions().setAddress(TEMPLATE_FEED)
    var outboundPermitted6 = PermittedOptions().setAddress(TEMPLATE_JSON)

    
    // Let through any messages from addresses starting with "news." (e.g. news.europe, news.usa, etc)
    /*var outboundPermitted2 = PermittedOptions()
      .setAddressRegex("news\\..+")*/

    // Let's define what we're going to allow from client -> server
    var options = BridgeOptions()
      .setInboundPermitted(Buffer(inboundPermitted1, inboundPermitted1, inboundPermitted3 , inboundPermitted4, inboundPermitted5, inboundPermitted6))
      .setOutboundPermitted(Buffer(outboundPermitted1, outboundPermitted2, outboundPermitted3 , outboundPermitted4, outboundPermitted5, outboundPermitted6))

    val sockJSHandler = SockJSHandler.create(vertx).bridge(options)
    
    // In order to use a Thymeleaf template we first need to create an engine
    val engine:ThymeleafTemplateEngine  = ThymeleafTemplateEngine.create(vertx)

    router.route("/eventbus/*").handler(sockJSHandler)
    router.route("/templateDef/*").handler(ctx => {
          val data = new JsonObject()
        .put("welcome", "Request processed at " + LocalDateTime.now())
      engine.render(data, "web/templateDef.html", res => {
        if (res.succeeded()) {
          ctx.response().end(res.result())
        } else {
          ctx.fail(res.cause())
        }
      })
        })
    router.route().handler(StaticHandler.create(STATIC_CONTENT_PATH))

    // Bind respective end points

    vertx //<4>
      .createHttpServer()
      .requestHandler(router.accept)
      .listenFuture(8009, "0.0.0.0") // <5>
      .map(_ => ()) // <6>
      
      
      
  }

  override def stop(): Unit = {
    println("Stopping")
  }

}