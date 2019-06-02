package com.stats.restverticle;

import com.stats.restservice.application.FilePathProcessor;
import com.stats.restservice.external.services.IStatisticsService;
import com.stats.restservice.external.services.ITransactionService;
import com.stats.restservice.internal.services.StatisticsServiceImpl;
import com.stats.restservice.internal.services.TransactionServiceImpl;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Verticle application starter from command line through maven commands
 * eg: mvn exec test:java //will deploy all the verticles 
 */
public class RestVerticleApplicationStarter extends AbstractVerticle {
	
	/**
	 * Invoked when this verticle is deployed. (Life cycle method to start the verticle) 
	 */
	@Override
	public void start() {
		Vertx vertx = Vertx.vertx();
		Logger logger = LoggerFactory.getLogger(RestVerticleApplicationStarter.class);
		
		IStatisticsService statService = new StatisticsServiceImpl();
		ITransactionService transactionService = new TransactionServiceImpl(statService);
		logger.info("Transaction and Statistics Services initialized successfully");
		
		RestAPIVerticle restVerticle = new RestAPIVerticle(transactionService , statService);
		logger.info("deploying RestAPIVerticle - Event loop initiating...");
		
		PeriodicCleanerVerticle cleanerVerticle = new PeriodicCleanerVerticle(statService , new FilePathProcessor());
		logger.info("deploying PeriodicCleanerVerticle - Periodic Event loop initiating...");
		
		vertx.deployVerticle(restVerticle);
		vertx.deployVerticle(cleanerVerticle);
		logger.info("Successfully deployed all verticles at localhost:8080");
	}
}
