package com.stats.restservice.transaction;

/**
 * Interface to access Transaction Data 
 */
public interface ITransaction {
	
	/*enum TransactionType {
		BASIC,ADVANCED;
	}*/

	/**
	 * Fetches the amount value of a Transaction
	 * @return - Double 
	 */
	Double getAmount();

	/**
	 * Fetches the timestamp of the transaction
	 * @return
	 */
	Long getTimestamp();

	String getTransactionPackage();

}
