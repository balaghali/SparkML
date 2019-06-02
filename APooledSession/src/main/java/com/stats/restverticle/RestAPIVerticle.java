package com.stats.restverticle;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import com.adolf.pool.manager.MyException;
import com.adolf.pool.manager.PackageData;
import com.adolf.pool.manager.RequestorPoolManager;
import com.stats.restservice.external.services.IStatisticsService;
import com.stats.restservice.external.services.ITransactionService;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;

public class RestAPIVerticle extends AbstractVerticle {
	static final String TRANSACTION_COMPLETED_END_POINT = "/transactionCompletedCount";
	static final String ADD_TRANSACTION_END_POINT = "/transactions";
	static final String GET_REQUESTOR_POOL_END_POINT = "/poolInfo";
	static final String GET_STATS_END_POINT = "/statistics";
	static final String CONTENT_TYPE = "content-type";
	static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json; charset=utf-8";
	private ITransactionService mTransactionService;
	private IStatisticsService mStatistcsService;
	private static final Logger logger = LoggerFactory.getLogger(RestAPIVerticle.class);

	private AtomicLong mTransactionCounter = new AtomicLong(0);
	private ExecutorService mExecutor = Executors.newFixedThreadPool(6);
	CompletableFuture<Optional<Boolean>> mFutureResponse = null;

	private static final String ROUTE_PATH = "/eventbus/*";
	private static final String STATIC_CONTENT_PATH = "web";
	private int _port = 8080;
	private static final String EVENTS_CONFIG = "events-config";
	private static final String EVENTS_JSON = "events-json";
	private static final String EVENTS_FEED = "events-feed";

	private static final String POOL_CONFIG = "pool-config";
	private static final String POOL_JSON = "pool-json";
	private static final String POOL_FEED = "pool-feed";

	public RestAPIVerticle(ITransactionService transactionService, IStatisticsService statisticsService) {
		mTransactionService = transactionService;
		mStatistcsService = statisticsService;
		initPools(3, 3, 2);
	}

	private void initPools(int mPoolMaxIdle, int mPoolMaxActive, int mPoolMaxWait) {
		IntStream.range(1, 2).forEach(i -> {
			try {
				String packageName = String.valueOf(i);
				RequestorPoolManager.getInstance().clearAllRequestorPools(packageName);
				com.adolf.pool.manager.RequestorPoolManager.getInstance().getOrInitPool(packageName,
						new PackageData(mPoolMaxIdle, mPoolMaxActive, mPoolMaxWait));
			} catch (MyException e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Invoked when this verticle is deployed. (Life cycle method to start the
	 * verticle)
	 */
	@Override
	public void start(Future<Void> fut) {

		// Create a router object.
		Router router = Router.router(vertx);

		// Bind "/" to our hello message - so we are still compatible.
		/*
		 * router.route("/").handler(routingContext -> { HttpServerResponse response =
		 * routingContext.response(); response .putHeader(CONTENT_TYPE, "text/html")
		 * .end("<h1>Welcome to statistics application</h1>"); });
		 */

		// Bind body handler to fetch the json body of post requests
		router.route().handler(BodyHandler.create());

		// Bind respective end points
		router.get(GET_STATS_END_POINT).handler(this::getStats);
		router.post(ADD_TRANSACTION_END_POINT).handler(this::addTransaction);
		router.get(GET_REQUESTOR_POOL_END_POINT).handler(this::getPoolInfo);
		router.get(TRANSACTION_COMPLETED_END_POINT).handler(this::getTransactionCount);

		BridgeOptions options = new BridgeOptions().addOutboundPermitted(new PermittedOptions().setAddress(EVENTS_FEED))
				.addOutboundPermitted(new PermittedOptions().setAddress(EVENTS_JSON))
				.addOutboundPermitted(new PermittedOptions().setAddress(EVENTS_CONFIG))
				.addInboundPermitted(new PermittedOptions().setAddress(EVENTS_CONFIG))
				.addOutboundPermitted(new PermittedOptions().setAddress(POOL_FEED))
				.addOutboundPermitted(new PermittedOptions().setAddress(POOL_JSON))
				.addOutboundPermitted(new PermittedOptions().setAddress(POOL_CONFIG))
				.addInboundPermitted(new PermittedOptions().setAddress(POOL_CONFIG))
				.addInboundPermitted(new PermittedOptions().setAddress(ADD_TRANSACTION_END_POINT))
				.addInboundPermitted(new PermittedOptions().setAddress(TRANSACTION_COMPLETED_END_POINT))
				.addInboundPermitted(new PermittedOptions().setAddress(GET_STATS_END_POINT))
				.addInboundPermitted(new PermittedOptions().setAddress(GET_REQUESTOR_POOL_END_POINT));
		router.route(ROUTE_PATH).handler(SockJSHandler.create(vertx).bridge(options));

		router.route().handler(StaticHandler.create(STATIC_CONTENT_PATH));

		io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine engine = ThymeleafTemplateEngine.create(vertx);

		router.route("/poolDef/*").handler(ctx -> {
			// we define a hardcoded title for our application
			JsonObject data = new JsonObject().put("welcome", "Hi there!");
			// and now delegate to the engine to render it.
			engine.render(data, "web/poolDef.html", res -> {
				if (res.succeeded()) {
					ctx.response().end(res.result());
				} else {
					ctx.fail(res.cause());
				}
			});
		});

		ConfigRetriever retriever = ConfigRetriever.create(vertx);
		retriever.getConfig(config -> {
			if (config.failed()) {
				fut.fail(config.cause());
			} else {
				// Create the HTTP server and pass the "accept" method to the request handler.
				vertx.createHttpServer().requestHandler(router::accept).listen(
						// Retrieve the port from the configuration,
						// default to 8080.
						config.result().getInteger("HTTP_PORT", 8080), result -> {
							if (result.succeeded()) {
								fut.complete();
							} else {
								fut.fail(result.cause());
							}
						});
			}
		});
	}

	/**
	 * Blocking call to fetch the statistics in case if there is humungous input
	 * traffic, avoid blocking the event loop thread
	 * 
	 * @param routingContext
	 */
	private void getStats(RoutingContext routingContext) {
		vertx.executeBlocking(future -> {
			future.complete(mStatistcsService.getStatistics());
		}, res -> {
			if (res.succeeded()) {
				Object result = res.result();
				routingContext.response().putHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8)
						.end(Json.encodePrettily(result));
			} else {
				logger.info("Future completion of Get request encountered error");
				routingContext.response().putHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8).setStatusCode(500)
						.end();
			}
		});
	}

	private void getPoolInfo(RoutingContext routingContext) {
		vertx.executeBlocking(future -> {
			future.complete(RequestorPoolManager.getInstance().getCurrentState());
		}, res -> {
			if (res.succeeded()) {
				Object result = res.result();
				routingContext.response().putHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8)
						.end(Json.encodePrettily(result));
			} else {
				logger.info("Future completion of Get request encountered error");
				routingContext.response().putHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8).setStatusCode(500)
						.end();
			}
		});
	}

	private void getTransactionCount(RoutingContext routingContext) {
		vertx.executeBlocking(future -> {
			future.complete(mTransactionCounter.get());
		}, res -> {
			if (res.succeeded()) {
				Object result = res.result();
				routingContext.response().putHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8)
						.end(Json.encodePrettily(result));
			} else {
				logger.info("Future completion of Get request encountered error");
				routingContext.response().putHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8).setStatusCode(500)
						.end();
			}
		});
	}

	private void addTransaction(RoutingContext routingContext) {

		Supplier<JsonObject> jsonData = () -> routingContext.getBodyAsJson();
		/*
		 * Optional<Boolean> status =
		 * mTransactionService.unMarshallTransactionData(jsonData); if
		 * (status.isPresent()) { sendResponeWithStatus(routingContext,201); } else {
		 * sendResponeWithStatus(routingContext,204); }
		 */

		long value = mTransactionCounter.get();
		logger.info("Transaction number:" + value);

		mFutureResponse = CompletableFuture.supplyAsync(() -> {
			mTransactionCounter.incrementAndGet();
			// logger.info("Transaction number:"+value);
			Optional<Boolean> status = mTransactionService.unMarshallTransactionData(jsonData);
			long val = mTransactionCounter.decrementAndGet();
			if (val == 0) {
				logger.info("Received Transactions completed...");
			}
			return status;

		}, mExecutor).exceptionally(ex -> {
			logger.error("Encountered exception while publish data operation", ex.getMessage());
			logger.error(ex);
			return Optional.of(true); // can be extended in future to return an object to the caller
		});

		sendResponeWithStatus(routingContext, 201);

	}

	private void sendResponeWithStatus(RoutingContext routingContext, int statusCode) {
		routingContext.response().setStatusCode(statusCode).putHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8)
				.end();
	}

}
