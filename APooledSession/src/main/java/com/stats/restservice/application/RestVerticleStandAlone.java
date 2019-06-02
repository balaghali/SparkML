package com.stats.restservice.application;

import com.stats.restservice.external.services.IStatisticsService;
import com.stats.restservice.external.services.ITransactionService;
import com.stats.restservice.internal.services.StatisticsServiceImpl;
import com.stats.restservice.internal.services.TransactionServiceImpl;
import com.stats.restverticle.GraphVerticle;
import com.stats.restverticle.PeriodicCleanerVerticle;
import com.stats.restverticle.RestAPIVerticle;
import com.stats.restverticle.RestVerticleApplicationStarter;
import com.stats.restverticle.SockJSEventBusBridge;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Stand Alone java class with main method to run the application from IDE directly 
 */
public class RestVerticleStandAlone extends AbstractVerticle {
	
	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		Logger logger = LoggerFactory.getLogger(RestVerticleApplicationStarter.class);
		
		IStatisticsService statService = new StatisticsServiceImpl();
		ITransactionService transactionService = new TransactionServiceImpl(statService);
		logger.info("Transaction and Statistics Services initialized successfully");
		
		RestAPIVerticle restVerticle = new RestAPIVerticle(transactionService , statService);
		logger.info("deploying RestAPIVerticle - Event loop initiating...");
		
		FilePathProcessor dto = new FilePathProcessor(args);
		logger.info("FilePath supplier .." + dto);
		PeriodicCleanerVerticle cleanerVerticle = new PeriodicCleanerVerticle(statService , dto);
		logger.info("deploying PeriodicCleanerVerticle - Periodic Event loop initiating...");
		
		GraphVerticle graphVerticle = new GraphVerticle();
		logger.info("deploying GraphVerticle ...");

		
		vertx.deployVerticle(restVerticle);
		vertx.deployVerticle(cleanerVerticle);
		vertx.deployVerticle(graphVerticle);
        //vertx.deployVerticle(new SockJSEventBusBridge(args != null && args.length > 0 ? args[0] : "8080"));
		logger.info("Successfully deployed all verticles at localhost:8080");
	}
}


