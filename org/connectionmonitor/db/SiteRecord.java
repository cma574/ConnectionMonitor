package org.connectionmonitor.db;

import java.sql.Timestamp;

/**
 * An object to store results of a database query for email reporting.
 * @author Cory Ma
 */
public class SiteRecord
{
	private final String SITE_NAME;
	private final int STATUS_NUM;
	private final Timestamp PING_TIME;
	private final double LATENCY;
	
	/**
	 * Constructor.
	 * @param name            Name of the site
	 * @param siteStatus      Status number of the ping result
	 * @param time            Time of the ping
	 * @param siteLatency     Latency  of the ping
	 */
	public SiteRecord(String name, int siteStatus, Timestamp time, double siteLatency)
	{
		SITE_NAME = name;
		STATUS_NUM = siteStatus;
		PING_TIME = time;
		LATENCY = siteLatency;
	}
	
	/**
	 * Gets the SITE_NAME for the query result.
	 * @return The name of the site
	 */
	public String getSiteName()
	{
		return SITE_NAME;
	}
	
	/**
	 * Gets the STATUS_NUM for the query result.
	 * @return The status number of the ping command:
	 * 	        1 - Site unreachable
	 *          2 - Site reachable again
	 *          3 - Site Latency Slow
	 */
	public int getStatusNum()
	{
		return STATUS_NUM;
	}
	
	/**
	 * Gets the PING_TIME for the query result.
	 * @return The time the ping command
	 */
	public Timestamp getPingTime()
	{
		return PING_TIME;
	}
	
	/**
	 * Gets LATENCY for the query result.
	 * @return The latency of the ping command
	 */
	public double getLatency()
	{
		return LATENCY;
	}
}
