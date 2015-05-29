import java.sql.Timestamp;

/**
 * An object to store results of a database query for email reporting.
 * @author Cory Ma
 */
public class SiteRecord
{
	private final String siteName;
	private final int statusNum;
	private final Timestamp pingTime;
	private final double latency;
	
	/**
	 * Constructor.
	 * @param name            Name of the site
	 * @param siteStatus      Status number of the ping result
	 * @param time            Time of the ping
	 * @param siteLatency     Latency  of the ping
	 */
	public SiteRecord(String name, int siteStatus, Timestamp time, double siteLatency)
	{
		siteName = name;
		statusNum = siteStatus;
		pingTime = time;
		latency = siteLatency;
	}
	
	/**
	 * Gets the siteName for the query result.
	 * @return The name of the site
	 */
	public String getSiteName()
	{
		return siteName;
	}
	
	/**
	 * Gets the statusNum for the query result.
	 * @return The status number of the ping command:
	 * 	        1 - Site unreachable
	 *          2 - Site reachable again
	 *          3 - Site Latency Slow
	 */
	public int getStatusNum()
	{
		return statusNum;
	}
	
	/**
	 * Gets the pingTime for the query result.
	 * @return The time the ping command
	 */
	public Timestamp getPingTime()
	{
		return pingTime;
	}
	
	/**
	 * Gets latency for the query result.
	 * @return The latency of the ping command
	 */
	public double getLatency()
	{
		return latency;
	}
}
