package com.stats.restservice.internal.services;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import com.stats.restservice.external.services.IStatisticsService;
import com.stats.restservice.transaction.ITransaction;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * StatisticsService that takes care of calculation of statistics , api's to prune the entries etc 
 */
public class StatisticsServiceImpl implements IStatisticsService{

	static final String COUNT = "count";
	static final String MIN = "min";
	static final String MAX = "max";
	static final String AVERAGE = "avg";
	static final String SUM = "sum";
	
	 private static final Logger logger = LoggerFactory.getLogger(StatisticsServiceImpl.class);
	
	Set<ITransaction> mTransactionSet = null;
	
	public StatisticsServiceImpl() {
		// ConcurrentHashSet to be operated by multiple threads
		mTransactionSet = ConcurrentHashMap.newKeySet(1000);	
	}

	 @Override
	  public Map<String, Number> getStatistics(){
		 /**
		  * Can make the following stream() => parallelStream() depending on the traffic of input new transactions. IF the size of transactionSet is huge 
		  * like more than 5000 or so parallelStream may be beneficial to use (Premature optimization is evil ?? so stick with sequential stream for now)
		  */
		  DoubleSummaryStatistics summaryStats = mTransactionSet.stream()
				   //.filter(this::ignoreOlderEntries)
		           .mapToDouble(this::getAmount)
		           .summaryStatistics();
		  	
	        Map<String, Number> statistics = new LinkedHashMap<>();
			statistics.put(SUM, summaryStats.getSum());
	        statistics.put(AVERAGE, summaryStats.getAverage());
	        statistics.put(MAX, summaryStats.getMax());
	        statistics.put(MIN, summaryStats.getMin());
	        statistics.put(COUNT, summaryStats.getCount());
	        
	        logger.info("Statstics => "+statistics);
	        return  statistics;
	    }

	private boolean ignoreOlderEntries(ITransaction transaction) {
		return (System.currentTimeMillis() - transaction.getTimestamp()) / 1000 < 60;
	}

	protected Double getAmount(ITransaction transaction){
		return transaction.getAmount();
	}

	@Override
	public void computeTransaction(ITransaction transaction) {
		mTransactionSet.add(transaction);
	}

	@Override
	public void removeStaleTransactions() {
		mTransactionSet.removeIf(isOlderThanOneMinute());		
	}
	
	 protected Predicate<ITransaction> isOlderThanOneMinute() {
	        ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
	        long epochInMillis = utc.toEpochSecond() * 1000;
	        return transaction ->
	                transaction.getTimestamp() < epochInMillis - 60 * 1000l;
	   }
}
