package com.adolf.pool.manager;

interface RequestorPool {

	String getLastAccess();

	String getApplication();

	int getIdleCount();

	int getActiveCount();

	int getMostIdle();

	int getMostActive();

	int getMaxIdle();

	int getMaxActive();

	long getMaxWait();

	long getLongestWait();

	long getTimeouts();

	ISessionObject borrowRequestor() throws MyException;

	void returnRequestor(ISessionObject aRequestor) throws MyException;

	void keepRequestor(String aID) throws MyException;

	void clearRequestorPool() throws MyException;

	public void setPackageProperties(PackageData aProps);

	public void setPackageDefaults(String aPackageName);
	
	public void initializeRequestor(ISessionObject aRequestor) throws MyException;

}
