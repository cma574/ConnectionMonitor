package org.connectionmonitor.app;

/**
 * Provides the shutDown method to Threads to allow for the graceful shut down of the program.
 * @author Cory Ma
 */
public class ShutDownableThread extends Thread
{
	private boolean isShutDown = false;
	
	/**
	 * Constructor.
	 * @param name     Name of the Thread
	 */
	public ShutDownableThread(String name)
	{
		super(name);
	}
	
	/**
	 * Sets the isShutDown flag to insure the thread shuts down gracefully.
	 */
	public void shutDown()
	{
		isShutDown = true;
	}
	
	/**
	 * Gets the value of isShutDown.
	 * @return The value of isShutDown
	 */
	protected boolean isShutDown()
	{
		return isShutDown;
	}
}
