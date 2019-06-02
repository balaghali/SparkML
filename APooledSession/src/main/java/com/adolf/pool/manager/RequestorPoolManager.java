package com.adolf.pool.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestorPoolManager implements IRequestorPoolManager {

	private static final ConcurrentHashMap oPoolTable = new ConcurrentHashMap();

	private static final IRequestorPoolManager gInstance = new RequestorPoolManager();

	public static final IRequestorPoolManager getInstance() {
		return gInstance;
	}

	/**
	 * @param requestDesc
	 *            RequestDescriptor object with package name populated. If
	 *            tenant is populated in requestDesc, then that tenant will be
	 *            used, otherwise "shared".
	 */
	public ISessionObject borrowRequestor(RequestDescriptor requestDesc , PackageData aData) throws MyException {

		String packageName = requestDesc.getPackageName();

		String poolKey = packageName.toUpperCase() + ":SHARED";

		System.out.println("Getting pooled requestor for service package " + packageName);

		// Look for an existing requestor pool in the static table
		RequestorPool pool = (RequestorPool) oPoolTable.get(poolKey);

		// If necessary, create one and store it in the table
		if (pool == null)
			pool = createRequestorPool(packageName, aData);

		ISessionObject requestor = pool.borrowRequestor();
		// Return a handle to a valid requestor from the pool

		return requestor;
	}

	public boolean returnRequestor(String aPackageName, ISessionObject aRequestor) throws MyException {
		String poolKey = aPackageName.toUpperCase() + ":SHARED";
		// Get the pool associated with this package name
		RequestorPool pool = (RequestorPool) oPoolTable.get(poolKey);

		if (pool == null) {
			System.out.println("Destroying requestor for service package pool" + poolKey);
			return destroyRequestor(aRequestor);
		} else {
			System.out.println("Returning pooled requestor for service package " + aPackageName);
			// Return the requestor ID to the pool.
			// If the pool is full, the requestor will be destroyed.
			pool.returnRequestor(aRequestor);
			return true;
		}
	}

	public void keepRequestor(String aPackageName, String aID, String aTenantName) throws MyException {

		String poolKey = aPackageName.toUpperCase() + ":" + aTenantName.toUpperCase();

		// Get the pool associated with this package name
		RequestorPool pool = (RequestorPool) oPoolTable.get(poolKey);

		if (pool != null)
			pool.keepRequestor(aID);
	}

	@Override
	public ISessionObject borrowRequestor(String packageName , PackageData aData) throws MyException {
		RequestorPool pool = getOrInitPool(packageName , aData);
		ISessionObject requestor = pool.borrowRequestor();
		return requestor;
	}

	private String getKey(String aPackageName) {
		return aPackageName.toUpperCase() + ":SHARED";
	}

	@Override
	public RequestorPool getOrInitPool(String aPackageName, PackageData aData) throws MyException {
		String poolKey = getKey(aPackageName);

		// Look for an existing requestor pool in the static table
		RequestorPool pool = (RequestorPool) oPoolTable.get(poolKey);

		if (pool == null) {
			pool = createRequestorPool(aPackageName , aData);
		}
		return pool;
	}

	/**
	 * Clears the requestor pool of the current requestor with aPackageName
	 * Tenant ID is taken from current requestor
	 * 
	 * @param aPackageName
	 * @throws MyException
	 */
	public void clearRequestorPool(String aPackageName) throws MyException {

		String poolKey = aPackageName.toUpperCase() + ":SHARED";

		System.out.println("Clearing requestor pool for service package " + aPackageName);

		// Remove the pool associated with this package name
		RequestorPool pool = (RequestorPool) oPoolTable.remove(poolKey);

		if (pool != null) {

			// Clear all requestor ID's from the pool.
			// The pool will handle destuction of each requestor.
			pool.clearRequestorPool();
		}
	}

	@Override
	public boolean clear(String aRequestorPoolName) throws MyException {

		String aRequestorPoolKey = aRequestorPoolName;

		aRequestorPoolKey = aRequestorPoolName + ":SHARED";
		System.out.println("Clearing requestor pool for service package with key  " + aRequestorPoolKey);

		// Remove the pool associated with this package name
		RequestorPool pool = (RequestorPool) oPoolTable.remove(aRequestorPoolKey);

		if (pool != null) {
			// Clear all requestor ID's from the pool.
			// The pool will handle destuction of each requestor.
			pool.clearRequestorPool();
			return true;
		}
		return false;
	}

	/**
	 * This API clears all the requestor pools which match the package name,
	 * across all tenants
	 * 
	 * @param aPackageName
	 * @throws MyException
	 */
	public void clearAllRequestorPools(String aPackageName) throws MyException {
		// Iterate over all requestor pools, remove all the pools with this
		// package name
		Iterator qualifiedPkgs = oPoolTable.keySet().iterator();

		System.out.println("Clearing requestor pool(s) for service package " + aPackageName);
		while (qualifiedPkgs.hasNext()) {
			String poolKey = (String) qualifiedPkgs.next();
			// Get the requestor pool associated with this package name
			if ((poolKey).startsWith(aPackageName.toUpperCase())) {
				// Remove the pool associated with this package name
				RequestorPool pool = (RequestorPool) oPoolTable.remove(poolKey);

				if (pool != null) {

					// Clear all requestor ID's from the pool.
					// The pool will handle destruction of each requestor.
					pool.clearRequestorPool();
				}
			}
		}
	}

	public List<Map<String, Object>> getCurrentState() {

		List<Map<String, Object>> stateData = new ArrayList<>();
		List<RequestorPoolDetailsDTO> poolDataList = getRequestorPoolDetails();

		for (RequestorPoolDetailsDTO dataDTO : poolDataList) {
			Map<String, Object> poolData = new HashMap<>();

			poolData.put(INFO_SERVICE_PACKAGE, dataDTO.getServicePackageName());
			poolData.put(INFO_LAST_ACCESS, dataDTO.getLastAccessTime());
			poolData.put(INFO_IDLE_COUNT, dataDTO.getIdleCount());
			poolData.put(INFO_ACTIVE_COUNT,dataDTO.getActiveCount());
			poolData.put(INFO_MOST_IDLE,dataDTO.getMostIdleCount());
			poolData.put(INFO_MOST_ACTIVE,dataDTO.getMostActiveCount());
			poolData.put(INFO_MAX_IDLE,dataDTO.getMaxIdleCount());
			poolData.put(INFO_MAX_ACTIVE,dataDTO.getMaxActiveCount());
			poolData.put(INFO_MAX_WAIT,dataDTO.getMaxWaitTime());
			poolData.put(INFO_LONGEST_WAIT,dataDTO.getLongestWaitTime());
			poolData.put(INFO_TIMEOUTS,dataDTO.getTimeoutCount());
			poolData.put(INFO_APPLICATION,dataDTO.getApplicationInfo());

			stateData.add(poolData);
		}

		return stateData;
	}

	@Override
	public List<RequestorPoolDetailsDTO> getRequestorPoolDetails() {

		List<RequestorPoolDetailsDTO> stateData = new ArrayList();
		Map tempPoolTable = new HashMap(oPoolTable); // no clone on jsr166
														// ConcurrentHashMap

		// Iterate over all requestor pools, collecting state data
		Iterator qualifiedPkgs = tempPoolTable.keySet().iterator();
		while (qualifiedPkgs.hasNext()) {

			// Create a new hashmap for each requestor pool
			RequestorPoolDetailsDTO poolData = new RequestorPoolDetailsDTO();

			// Get the requestor pool associated with this package name
			String packageName = (String) qualifiedPkgs.next();
			RequestorPool pool = (RequestorPool) tempPoolTable.get(packageName);

			// Load pool data into DTO
			poolData.setServicePackageName(packageName);
			poolData.setLastAccessTime(pool.getLastAccess());
			poolData.setIdleCount(pool.getIdleCount());
			poolData.setActiveCount(pool.getActiveCount());
			poolData.setMostIdleCount(pool.getMostIdle());
			poolData.setMostActiveCount(pool.getMostActive());
			poolData.setMaxIdleCount(pool.getMaxIdle());
			poolData.setMaxActiveCount(pool.getMaxActive());
			poolData.setMaxWaitTime(pool.getMaxWait() / 1000);
			poolData.setLongestWaitTime(pool.getLongestWait() / 1000);
			poolData.setTimeoutCount(pool.getTimeouts());
			poolData.setApplicationInfo(pool.getApplication());

			stateData.add(poolData);
		}
		return stateData;
	}

	@Override
	public List<String> getAllServicePackages() {
		Map tempPoolTable = new HashMap(oPoolTable); 		return new ArrayList<String>(tempPoolTable.keySet());
	}

	private synchronized RequestorPool createRequestorPool(String aPackageName, PackageData aData)
			throws MyException {
			    
				aPackageName = aPackageName.toUpperCase();
			    String poolKey = aPackageName + ":SHARED";
			
				// Double-check that another thread hasn't just done this
				RequestorPool pool = (RequestorPool)oPoolTable.get(poolKey);
				if (pool != null)
					return pool;
			
			
				ISessionObject requestor = null;
			
				try {
			
						pool = new RequestorPoolCommonsV2(aData.getPoolMaxActive(), aData.getPoolMaxIdle() , aData.getPoolMaxWait());

//					requestor = pool.borrowRequestor();

					// If found, set the package properties on the pool
					if (aData != null) {
			
						// Cache package properties
						pool.setPackageProperties(aData);
						pool.setPackageDefaults(aPackageName);
//						pool.initializeRequestor(requestor);
					}
					// Cache the requestor pool in the table of requestor pools
					oPoolTable.put(poolKey, pool);
			
					return pool;
				}
				finally {
			
				}
			}

	public boolean destroyRequestor(ISessionObject aRequestorID) {

		boolean returnValue = false; 

				try {
					aRequestorID.reset();
					returnValue = true;
				} catch (Exception e) {
					System.out.println("Problem returning requestor to ServiceRequestorPool"+e);
				}
		return returnValue;
	}
	
	public RequestorPoolManager() {
		super();
	}

	@Override
	public ISessionObject borrowRequestor(RequestDescriptor requestDesc) throws MyException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISessionObject borrowRequestor(String packageName) throws MyException {
		RequestorPool pool;
		ISessionObject requestor = null ;
			pool = getOrInitPool(packageName , null);
			requestor = pool.borrowRequestor();
		return requestor;
	}

}

