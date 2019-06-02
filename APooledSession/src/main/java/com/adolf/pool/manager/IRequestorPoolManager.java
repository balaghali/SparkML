package com.adolf.pool.manager;

import java.util.List;
import java.util.Map;

public interface IRequestorPoolManager {


	public static final String INFO_SERVICE_PACKAGE = "PACKAGE_KEY";
	public static final String INFO_LAST_ACCESS = "LAST_ACCESS";
	public static final String INFO_IDLE_COUNT = "IDLE_COUNT";
	public static final String INFO_ACTIVE_COUNT = "ACTIVE_COUNT";
	public static final String INFO_MOST_IDLE = "MOST_IDLE";
	public static final String INFO_MOST_ACTIVE = "MOST_ACTIVE";
	public static final String INFO_MAX_IDLE = "MAX_IDLE";
	public static final String INFO_MAX_ACTIVE = "MAX_ACTIVE";
	public static final String INFO_MAX_WAIT = "MAX_WAIT";
	public static final String INFO_LONGEST_WAIT = "LONGEST_WAIT";
	public static final String INFO_TIMEOUTS = "TIMEOUTS";
	public static final String INFO_APPLICATION = "APPLICATION";

	public abstract ISessionObject borrowRequestor(RequestDescriptor requestDesc)
			throws MyException;

	public abstract boolean returnRequestor(String aPackageName, ISessionObject aRequestor) throws MyException;

	public abstract void keepRequestor(String aPackageName, String aID, String aTenantName) throws MyException;

	public abstract void clearRequestorPool(String aPackageName)
			throws MyException;

	public abstract void clearAllRequestorPools(String aPackageName)
		throws MyException;
	
	public abstract List getCurrentState();

	public abstract boolean destroyRequestor(ISessionObject iSessionObject);

	ISessionObject borrowRequestor(String packageName, PackageData aData) throws MyException;
	
	ISessionObject borrowRequestor(String packageName) throws MyException;
	
	/**
	 * Introduced for Operations API
	 * Return the list of Requestor pool current state information.
	 * return  List<RequestorPoolDetailsDTO>
	 */
	public List <RequestorPoolDetailsDTO> getRequestorPoolDetails();

	/**
	 * Introduced for Operations API
	 * Clear the Requestor pool for the given ServicePackage name and Node
	 * @param aPackageName Tenant qualified service package name to be cleared
	 * @throws MyException
	 */
	public boolean clear(String aServicePackageName) throws MyException;

	/**
	 * Introduced for Operations API
	 * Return the list of service packages for current node state
	 * @return List of service package names
	 */
	public List<String> getAllServicePackages();

	public RequestorPool getOrInitPool(String aPackageName, PackageData aData) throws MyException;
}
