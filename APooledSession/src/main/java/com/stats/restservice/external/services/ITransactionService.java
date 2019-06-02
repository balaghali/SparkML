package com.stats.restservice.external.services;

import java.util.Optional;
import java.util.function.Supplier;

import io.vertx.core.json.JsonObject;

/**
 * Interface to process the transactions received from external invocations 
 */
public interface ITransactionService {
	
	/**
	 * API to pre-process the transaction json data.   
	 * @param jsonData
	 * @return - Optional about the status of the operation 
	 */
	Optional<Boolean> unMarshallTransactionData(Supplier<JsonObject> jsonData);
	
}
