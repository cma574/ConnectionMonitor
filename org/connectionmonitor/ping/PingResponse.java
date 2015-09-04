package org.connectionmonitor.ping;
import java.util.Date;

/**
 * An object for holding the response of a ping.
 * @author Cory Ma
 */
public class PingResponse
{
	private final boolean reachable;
	private final String ip;
	private final double latency;
	private final Date pingTime;
	
	/**
	 * Constructor.
	 * @param isReachable     If the ping reached its destination
	 * @param ipAddress       IP address of the ping
	 * @param pingLatency     Latency of the ping
	 */
	public PingResponse(boolean isReachable, String ipAddress, double pingLatency)
	{
		reachable = isReachable;
		ip = ipAddress;
		latency = pingLatency;
		pingTime = new Date();
	}
	
	/**
	 * Gets whether the ping reached its destination.
	 * @return true if the site was reachable, false if not
	 */
	public boolean getReachable()
	{
		return reachable;
	}
	
	/**
	 * Gets the IP address of the ping.
	 * @return The IP address of the ping
	 */
	public String getIP()
	{
		return ip;
	}
	
	/**
	 * Gets the latency of the ping.
	 * @return The latency of the ping
	 */
	public double getLatency()
	{
		return latency;
	}
	
	/**
	 * Gets the time of the ping.
	 * @return The time of the ping
	 */
	public Date getPingTime()
	{
		return pingTime;
	}
}