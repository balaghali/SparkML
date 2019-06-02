package com.adolf.pool.manager;

public class PackageData {
	
/*pyPoolMaxIdle
int maxActive = aPackagePage.getProperty("pyPoolMaxActive").toInteger();
mPool.setMaxWaitMillis(aPackagePage.getInteger("pyPoolMaxWait") 
*/
	
	private int mPoolMaxIdle;
	private int mPoolMaxActive;
	private int mPoolMaxWait;
	public PackageData(int mPoolMaxIdle, int mPoolMaxActive, int mPoolMaxWait) {
		super();
		this.mPoolMaxIdle = mPoolMaxIdle;
		this.mPoolMaxActive = mPoolMaxActive;
		this.mPoolMaxWait = mPoolMaxWait;
	}
	@Override
	public String toString() {
		return "PackagePage [mPoolMaxIdle=" + mPoolMaxIdle + ", mPoolMaxActive=" + mPoolMaxActive + ", mPoolMaxWait="
				+ mPoolMaxWait + "]";
	}
	public int getPoolMaxIdle() {
		return mPoolMaxIdle;
	}
	public int getPoolMaxActive() {
		return mPoolMaxActive;
	}
	public int getPoolMaxWait() {
		return mPoolMaxWait;
	}
	
}
