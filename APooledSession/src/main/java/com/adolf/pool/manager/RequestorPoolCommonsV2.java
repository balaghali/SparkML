package com.adolf.pool.manager;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class RequestorPoolCommonsV2 implements RequestorPool {

	// Requestor pool default settings
	private static final int MAX_IDLE = 10;
	private static final int MAX_ACTIVE = 10;
	private static final long MAX_WAIT = 10000;

	// Object pool, for holding requestor ID's
	private final GenericObjectPool mPool;
	
	private final Map<String,ISessionObject> mRequestorMap = new ConcurrentHashMap<>();

	// Track the initial application used by the pool
	private String mApplication = "";

	private volatile String mLastAccess = "";

	// Track the highest number of idle requestors since the pool was created
	private volatile int mMostIdle = 0;

	// Track the highest number of active requestors since the pool was created
	private volatile int mMostActive = 0;

	// Track the longest wait for a requestor since the pool was created
	private volatile long mLongestWait = 0;

	// Track the number of thread timeouts since the pool was created
	private AtomicLong mTimeouts = new AtomicLong(0);

	// end of diagnostic statistics

	private String mTenantName = "shared";

	private RequestorFactory mRequestorFactory;
	
	public RequestorPoolCommonsV2() {
		this(MAX_IDLE, MAX_ACTIVE, MAX_WAIT);
	}

	// Requestor factory helper class
	private class RequestorFactory implements PooledObjectFactory<String> {

		RequestorPool mParent;
		ThreadLocal<Boolean> mKeepRequestor = new ThreadLocal<Boolean>();

		// Constructor
		public RequestorFactory(RequestorPool aParent) {
			mParent = aParent;
		}

		public PooledObject<String> makeObject() throws MyException {

			ISessionObject requestor = null;

			// Use the system node to create a new requestor instance
			try {
				requestor = ISessionObject.newObject();
			} catch (Exception e) {
				throw new MyException(e);
			} finally {
			}

			String requestorId = null;

			if (requestor != null) {
				requestorId = requestor.getId();
			} else {
				throw new MyException("Failed to acquire new Requestor");
			}

			PooledString pooledString = new PooledString(requestorId);
			

			System.out.println("makeObject: " + requestorId);
			return pooledString;
		}

		public void destroyObject(PooledObject<String> aObj) throws MyException {
			System.out.println("destroyObject init: " + aObj.getObject());
			RequestorPoolManager.getInstance().destroyRequestor(mRequestorMap.get(aObj.getObject()));
			mRequestorMap.remove(aObj.getObject());
		}

		public boolean validateObject(PooledObject<String> aObj) {
			return true;
		}

		public void activateObject(PooledObject<String> aObj) throws Exception {
			// Not used
		}

		public void passivateObject(PooledObject<String> aObj) throws Exception {

		}

		public Object borrowRequestorInner(ISessionObject aRequestor) {
			aRequestor.updateLastAccess(); // try to prevent timeout until next
											// lock
											// request below
			try {
				initializeRequestor(aRequestor);
			} catch (MyException e) {
				//throw e;
			}
			return aRequestor;
		}

	}

		// Package private, for jUnit testing
	@SuppressWarnings("rawtypes")
	public RequestorPoolCommonsV2(int aMaxIdle, int aMaxActive, long aMaxWait) {

		int maxActive = aMaxActive;
		if (maxActive < 0) {
			maxActive = Integer.MAX_VALUE;
		}
		// Create a new object pool to hold requestor ID's
		GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
		cfg.setMaxIdle(aMaxIdle);
		cfg.setMaxWaitMillis(aMaxWait);
		cfg.setMaxTotal(maxActive);
		cfg.setTestOnBorrow(true);
		cfg.setTestOnCreate(false);
		cfg.setBlockWhenExhausted(true);

		this.mRequestorFactory = new RequestorFactory(this);

		mPool = new GenericObjectPool(mRequestorFactory, cfg);
	}

		// public only for getRunnableMethod
		public static Object updateLastAccess(ISessionObject aRequestor) {
			aRequestor.updateLastAccess(); // keep alive
			return aRequestor;
		}

		/*
		 * @Override public ServicePackage getPackageData() {
		 * 
		 * if (mPackageData != null) { return mPackageData.copy(); } else {
		 * return null; } }
		 */

		@Override
		public String getLastAccess() {
			return mLastAccess;
		}

		@Override
		public String getApplication() {
			return mApplication;
		}

		@Override
		public int getIdleCount() {
			return mPool.getNumIdle();
		}

		@Override
		public int getActiveCount() {
			return mPool.getNumActive();
		}

		@Override
		public int getMostIdle() {
			return mMostIdle;
		}

		@Override
		public int getMostActive() {
			return mMostActive;
		}

		@Override
		public int getMaxIdle() {
			return mPool.getMaxIdle();
		}

		@Override
		public int getMaxActive() {
			return mPool.getMaxTotal();
		}

		@Override
		public long getMaxWait() {
			return mPool.getMaxWaitMillis();
		}

		@Override
		public long getLongestWait() {
			return mLongestWait;
		}

		@Override
		public long getTimeouts() {
			return mTimeouts.get();
		}

	@Override
	public ISessionObject borrowRequestor() throws MyException {


		String poolId = null;

		// Reset the last access timestamp
		mLastAccess = LocalDateTime.now().toString();

		// Check if we need to update most idle count
		int idleCount = mPool.getNumIdle();
		if (idleCount > mMostIdle) {
			mMostIdle = idleCount;
		}

		// Get the current system time
		long startTime = System.currentTimeMillis();
		System.out.println("waiters:"+mPool.getNumWaiters()+", wait time"+mPool.getMaxWaitMillis());
		try {
			poolId = (String) mPool.borrowObject();
		} catch (Exception e) {
			mTimeouts.incrementAndGet();
			String msg = "Timed out borrowing service requestor from requestor pool , timeout set to " + mPool.getMaxWaitMillis();

			System.out.println(msg);
			throw new MyException(msg);
		}

		// Check if we need to update longest wait time
		long elapsedTime = System.currentTimeMillis() - startTime;
		if (elapsedTime > mLongestWait) {
			mLongestWait = elapsedTime;
		}

		// Check if we need to update most active count
		int activeCount = mPool.getNumActive();
		if (activeCount > mMostActive) {
			mMostActive = activeCount;
		}

		ISessionObject requestor = null;
		try {
			if(!mRequestorMap.containsKey(poolId)){
				requestor = ISessionObject.newObject();
				requestor.setPoolId(poolId);
				mRequestorMap.put(poolId, requestor);
			}
			else 
				requestor = mRequestorMap.get(poolId);
		} catch (Exception t) {
		}

		// Sanity check
		if (requestor == null) {
			throw new MyException("Could not acquire valid requestor from requestor pool");
		}
		
		requestor.updateLastAccess();

		System.out.println("borrowSession: " + poolId);
		
		return requestor;

	}

	@Override
	public void returnRequestor(ISessionObject aRequestor) throws MyException {

		String poolId = aRequestor.getPoolId();

		try {
			mPool.returnObject(poolId);
			mRequestorMap.remove(poolId);
		} catch (Exception e) {
			String msg = "Caught exception returning requestor to pool";
			System.out.println(msg);
			throw new MyException(e);
		}
	}

	@Override
	public void keepRequestor(String aID) throws MyException {

		System.out.println("Invalidating requestor in the pool");

		try {
			mPool.invalidateObject(aID);
		} catch (

		Exception e) {
			String msg = "Caught exception invalidating requestor in the pool";
			System.out.println(msg);
			throw new MyException(msg);
		}
	}

	@Override
	public void clearRequestorPool() throws MyException {

		try {
			// Clear the requestor pool
			mPool.clear();
		} catch (Exception e) {
			String msg = "Caught exception clearing requestor pool: " + e;
			System.out.println(msg);
			throw new MyException(msg);
		}
	}

	@Override
	public void setPackageProperties(PackageData aPackagePage) {

		// Cache the package data
//		 mPackageData = aPackagePage;

		// Reset the pool properties
		mPool.setMaxIdle(aPackagePage.getPoolMaxIdle());
		int maxActive = aPackagePage.getPoolMaxActive();
		if (maxActive < 0) {
			maxActive = Integer.MAX_VALUE;
		}
		mPool.setMaxTotal(maxActive);
		mPool.setMaxWaitMillis(aPackagePage.getPoolMaxWait() * 1000);
	}

	/*
	 * public void setPackageDefaults(String aPackageName) {
	 * 
	 * // Cache the package data mPackageData = new
	 * ServicePackageImpl(aPackageName); }
	 */

	public void initializeRequestor(ISessionObject aRequestor) throws MyException {

		// if (mPackageData != null) {
		aRequestor.setClientIPAddress(
				"127.0.0.1"/* mPackageData.getPackageName() */);

		// }
	}

	@Override
	public void setPackageDefaults(String aPackageName) {
		// TODO Auto-generated method stub
		
	}

	
	public static void main(String[] args) throws IOException {
		
	}
}
class PooledString extends DefaultPooledObject<String> { 
	

	public PooledString(String object) {
        super(object);
    }

 
}