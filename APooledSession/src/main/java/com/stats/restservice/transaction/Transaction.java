package com.stats.restservice.transaction;

/**
 * Transaction POJO class associated with received data to create transactions 
 */
public class Transaction implements ITransaction{
	
	private Double amount;
	private Long timestamp;
	private String mPackage;
	
	public Transaction(Double amount , Long timestamp, String transcationPackage) {
		this.amount = amount;
		this.timestamp = timestamp;
		mPackage = transcationPackage;
	}
	
	@Override
	public Double getAmount() {
		return amount; 
	}
	
	@Override
	public Long getTimestamp() {
		return timestamp; 
	}
	
	@Override
	public String getTransactionPackage() {
		return mPackage;
	}
	
	 @Override
	    public String toString() {
	        final StringBuilder sb = new StringBuilder("Transaction{");
	        sb.append("amount=").append(amount);
	        sb.append(", timestamp=").append(timestamp);
	        sb.append('}');
	        return sb.toString();
	    }
	
	 @Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((amount == null) ? 0 : amount.hashCode());
			result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Transaction other = (Transaction) obj;
			if (amount == null) {
				if (other.amount != null)
					return false;
			} else if (!amount.equals(other.amount))
				return false;
			if (timestamp == null) {
				if (other.timestamp != null)
					return false;
			} else if (!timestamp.equals(other.timestamp))
				return false;
			return true;
		}
}
