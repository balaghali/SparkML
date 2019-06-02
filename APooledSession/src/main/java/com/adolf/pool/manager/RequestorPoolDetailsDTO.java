package com.adolf.pool.manager;

public class RequestorPoolDetailsDTO {

	private int mActiveCount;
	@Override
	public String toString() {
		return "SessionPoolDetailsDTO [mActiveCount=" + mActiveCount + ", mIdleCount=" + mIdleCount
				+ ", mServicePackageName=" + mServicePackageName + ", mTimeoutCount=" + mTimeoutCount + "]";
	}

	private int mIdleCount;
	private String mLastAccessTime;
	private long mLongestWaitTime;
	private int mMaxActiveCount;
	private int mMaxIdleCount;
	private long mMaxWaitTime;
	private int mMostActiveCount;
	private int mMostIdleCount;
	private String mServicePackageName;
	private long mTimeoutCount;
	private String mApplicationInfo;

	public int getActiveCount() {
		return mActiveCount;
	}

	public void setActiveCount(int activeCount) {
		mActiveCount = activeCount;
	}

	public int getIdleCount() {
		return mIdleCount;
	}

	public void setIdleCount(int idleCount) {
		mIdleCount = idleCount;
	}

	public String getLastAccessTime() {
		return mLastAccessTime;
	}

	public void setLastAccessTime(String lastAccessTime) {
		mLastAccessTime = lastAccessTime;
	}

	public long getLongestWaitTime() {
		return mLongestWaitTime;
	}

	public void setLongestWaitTime(long longestWaitTime) {
		mLongestWaitTime = longestWaitTime;
	}

	public int getMaxActiveCount() {
		return mMaxActiveCount;
	}

	public void setMaxActiveCount(int maxActiveCount) {
		mMaxActiveCount = maxActiveCount;
	}

	public int getMaxIdleCount() {
		return mMaxIdleCount;
	}

	public void setMaxIdleCount(int maxIdleCount) {
		mMaxIdleCount = maxIdleCount;
	}

	public long getMaxWaitTime() {
		return mMaxWaitTime;
	}

	public void setMaxWaitTime(long maxWaitTime) {
		mMaxWaitTime = maxWaitTime;
	}

	public int getMostActiveCount() {
		return mMostActiveCount;
	}

	public void setMostActiveCount(int mostActiveCount) {
		mMostActiveCount = mostActiveCount;
	}

	public int getMostIdleCount() {
		return mMostIdleCount;
	}

	public void setMostIdleCount(int mostIdleCount) {
		mMostIdleCount = mostIdleCount;
	}

	public String getServicePackageName() {
		return mServicePackageName;
	}

	public void setServicePackageName(String servicePackageName) {
		mServicePackageName = servicePackageName;
	}

	public long getTimeoutCount() {
		return mTimeoutCount;
	}

	public void setTimeoutCount(long timeoutCount) {
		mTimeoutCount = timeoutCount;
	}

	public String getApplicationInfo() {
		return mApplicationInfo;
	}

	public void setApplicationInfo(String applicationInfo) {
		mApplicationInfo = applicationInfo;
	}

}