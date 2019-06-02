package com.adolf.pool.manager;

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.RandomStringUtils;

import com.adolf.pool.manager.RequestDescriptor.RequestorType;

public interface ISessionObject {

	static ISessionObject newObject() {
		return new SessionObject("Germany-" + System.currentTimeMillis(), RequestorType.BATCH);
	}

	String getPoolId();

	String getId();

	void updateLastAccess();

	void setClientIPAddress(String string);

	void reset();

	void performOpeartion();

	void setPoolId(String requestorId);

}

class SessionObject implements ISessionObject {

	private String mName;
	private RequestorType type;
	private String mId;
	protected volatile long mLastAccess;
	private String mClientAddress;
	private String mPoolId;
	
	public SessionObject(String string, RequestorType batch) {
		mName = string;
		type = batch;
		mId = RequestorIdGenerator.getInstance().generate("PK-");
	}

	@Override
	public String getId() {
		return mId;
	}

	@Override
	public void updateLastAccess() {
		mLastAccess = System.currentTimeMillis();
	}

	@Override
	public void setClientIPAddress(String string) {
		mClientAddress = string;
	}

	@Override
	public String toString() {
		return "SessionObject [mName=" + mName + ", type=" + type + ", mId=" + mId + ", mLastAccess=" + mLastAccess
				+ ", mClientAddress=" + mClientAddress + "]";
	}

	@Override
	public void reset() {
		mName = null;
		mLastAccess = -1;
		mClientAddress = null;
		mId = null;
		mClientAddress = null;
	}

	@Override
	public void performOpeartion() {
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void setPoolId(String requestorId) {
		mPoolId = requestorId;
	}
	
	@Override
	public String getPoolId() {
		return mPoolId;
	}

}

/**
 * This class is used to generate the Requestor ID using SecureRandom. To
 * generate RequestorID we use {@code random} method of
 * {@link RandomStringUtils} which accepts {@Code SecureRandom} instance as
 * input. This will generate RequestorID of length 33 alphanumeric characters
 * (Uppercase) to match with current implementation of Requestor ID generation.
 * 
 * This is giving entropy of 139 bits (Calculated using BURP Sequencer). If we
 * want to increase the entropy, increase the length of the Requestor ID,
 * Include lower case alphabets as well.
 * 
 * RequestorIdGenerator
 */
class RequestorIdGenerator {

	private static char[] ALLOWED_CHARS_ARRAY = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
	private static final long LIFESPAN = 100000;

	private SecureRandom mRandom;

	private static class RequestorIdGeneratorHolder {
		public static final RequestorIdGenerator instance = new RequestorIdGenerator();

		private RequestorIdGeneratorHolder() {
			// Dummy constructor to avoid sonar lint violation issue
		}
	}

	public static RequestorIdGenerator getInstance() {
		return RequestorIdGeneratorHolder.instance;
	}

	RequestorIdGenerator() {
		mRandom = new RequestorIdRandom(LIFESPAN);
	}

	public String generate(String requestorTypeCode) {
		String requestorId = requestorTypeCode
				+ RandomStringUtils.random(10, 0, 0, true, true, ALLOWED_CHARS_ARRAY, mRandom);
		return requestorId;
	}

	/**
	 * Wrapper to SecureRandom to handle re-seeding. This class is being used as
	 * input to {@link RandomStringUtils}, which uses nextInt() for random
	 * string generation, Note: We need to change this class if
	 * RandomStringUtils.random implementation changes.
	 * 
	 */
	private static class RequestorIdRandom extends SecureRandom {

		private static final long serialVersionUID = -7122817704167701670L;
		private AtomicLong mCounter;

		public RequestorIdRandom(long aCounter) {
			mCounter = new AtomicLong(aCounter);
		}

		@Override
		public int nextInt() {
			checkToReseed();
			return super.nextInt();
		}

		@Override
		public int nextInt(int n) {
			checkToReseed();
			return super.nextInt(n);
		}

		void checkToReseed() {
			if (mCounter.decrementAndGet() <= 0) {
				reseedGenerator();
			}
		}

		private void reseedGenerator() {
			mCounter.set(LIFESPAN);
			this.setSeed(this.generateSeed(32));
		}
	}

}
