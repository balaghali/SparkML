package com.stats.restservice.external.services;

import java.util.Map;

import com.stats.restservice.transaction.ITransaction;

/**
 *  Interface exposed to outside world to fetch the Statistics and to cleanse the older transactions
 */
public interface IStatisticsService {

	/**
	 * API to fetch that calculates , computes the transaction statistics in the past 60 seconds 
	 * @return the statistics map with metrics
	 */
	Map<String, Number> getStatistics();

	/**
	 * API to process/store the transaction  
	 * @param {@link ITransaction}
	 */
	void computeTransaction(ITransaction transaction);

	/**
	 * API to delete the expired transactions
	 * Usage: Internal - Used by PeriodicCleanerVerticle 
	 */
	void removeStaleTransactions();
}
