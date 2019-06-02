
package com.adolf.pool.manager;

public class RequestDescriptor {
	
	/** Enum-enabled handling of the request type strings listed above. */
	public enum RequestorType {

		APP("Application"),
		BATCH("Batch"),
		BROWSER("Browser"),
		PORTAL("Portal"),
		ASYNCPROCESSOR("AsyncProcessor");
		
		private String displayValue;

		RequestorType(String text) {
			this.displayValue = text;
		}
		
		public static RequestorType toEnum(String stringValue) {
			if(stringValue == null) {
				return null;
			}

			return RequestorType.valueOf(stringValue);
		}
		
		public String getDisplayValue() {
			return displayValue;
		}
	}

	public final static String INFO_DELIMITER = ";";
	
	private RequestorType reqType = null;
	private String packageName = null;
	
	public RequestDescriptor(RequestorType reqType) {
		this(reqType, null);
	}
	
	public RequestDescriptor(RequestorType reqType, String packageName) {
		this.reqType = reqType;
		this.packageName = packageName;
	}
	
	public RequestorType getReqType() {
		return reqType;
	}
	
	public String getPackageName() {
		return packageName;
	}
	
	
	
	public static RequestDescriptor toRequestDescriptor(String aInfo) {
		
			System.out.println("RequestDescriptor toRequestDescriptor(String) " + aInfo);
		
			return new RequestDescriptor(RequestorType.BATCH, "Germany");
		
	}
	
	
}
