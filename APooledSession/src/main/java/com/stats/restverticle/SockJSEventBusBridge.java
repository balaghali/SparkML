package com.stats.restverticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

public class SockJSEventBusBridge extends AbstractVerticle {

	private static final String ROUTE_PATH = "/eventbus/*";
	private static final String STATIC_CONTENT_PATH = "web";
	private int _port = 8080;
	private static final String EVENTS_CONFIG = "events-config";
	private static final String EVENTS_JSON = "events-json";

	private static final String EVENTS_FEED = "events-feed";
	private static final Logger LOG = LoggerFactory.getLogger(SockJSEventBusBridge.class);
	
	public SockJSEventBusBridge(String port) {
		_port = port != null && port.length() > 3 ? Integer.valueOf(port) : _port;
	}
	
	@Override
	public void start() {
		LOG.info("Configuring routes and setting permisssions.. Vertx");
		configureEventBusBrige();
		LOG.info("Access TelemetryDataGenerator @- http://localhost:"+_port);
	}

	private void configureEventBusBrige() {
		Router router = Router.router(vertx);

		BridgeOptions options = new BridgeOptions()
				.addOutboundPermitted(new PermittedOptions().setAddress(EVENTS_FEED))
				.addOutboundPermitted(new PermittedOptions().setAddress(EVENTS_JSON))
				.addOutboundPermitted(new PermittedOptions().setAddress(EVENTS_CONFIG))
				.addInboundPermitted(new PermittedOptions().setAddress(EVENTS_CONFIG));
		router.route(ROUTE_PATH).handler(SockJSHandler.create(vertx).bridge(options));

		router.route().handler(StaticHandler.create(STATIC_CONTENT_PATH));

		HttpServer server = vertx.createHttpServer();
		server.requestHandler(router::accept).listen(_port); //FIXME: should be a configurable option

	}

}
