package com.adolf.pool.manager;

public class MyException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2740360495532962484L;

	/**
	 * Default constructor with no message.
	 */
	public MyException() {
	}

	/**
	 * Creates an exception that is caused by a particular
	 * <code>Throwable</code>.
	 *
	 * @param aCause the <code>Throwable</code> that caused this
	 * 				 exception
	 */
	public MyException(Throwable aCause) {
		super("help", aCause);
	}


    public MyException(String string) {
		super(string);
	}

	/**
     * Returns a (short) description of this throwable.
     * (used by Throwable.printStackTrace to describe the exception)
     * @return a string representation of this throwable.
     */
    public String toString() {
    	return "exception";
    }
	
}
