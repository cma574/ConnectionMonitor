/**
 * An object to store information for locations to run the ping command on.
 * @author Cory Ma
 */
public class PingSite
{
	private final String NAME, ADDRESS;
	private double avgLatency, latencyStdDeviation;
	private int deviationTolerance;
	
	/**
	 * Constructor.
	 * @param siteName                    Name of the site
	 * @param siteAddress                 Address of the site
	 * @param siteAvgLatency              Average latency for pings of the site
	 * @param siteLatencyStdDeviation     Standard deviation for the average latency
	 * @param siteDeviationTolerance      Number of standard deviations before a ping is considered slow
	 */
	public PingSite(String siteName, String siteAddress, double siteAvgLatency, double siteLatencyStdDeviation, int siteDeviationTolerance)
	{
		NAME = siteName;
		ADDRESS = siteAddress;
		avgLatency = siteAvgLatency;
		latencyStdDeviation = siteLatencyStdDeviation;
		deviationTolerance = siteDeviationTolerance;
	}

	/**
	 * Gets the NAME of the site.
	 * @return The name of the site
	 */
	public String getName()
	{
		return NAME;
	}

	/**
	 * Gets the ADDRESS of the site.
	 * @return The address of the site
	 */
	public String getAddress()
	{
		return ADDRESS;
	}

	/**
	 * Gets the average latency of the site.
	 * @return The average latency of the site
	 */
	public double getAvgLatency()
	{
		return avgLatency;
	}

	/**
	 * Gets the standard deviation of the average latency of the site.
	 * @return The standard deviation of the average latency of the site
	 */
	public double getLatencyStdDeviation()
	{
		return latencyStdDeviation;
	}

	/**
	 * Gets the deviation tolerance of the site.
	 * @return The deviation tolerance of the site
	 */
	public int getDeviationTolerance()
	{
		return deviationTolerance;
	}

	/**
	 * Sets the average latency of the site.
	 * @param siteAvgLatency     Average latency for pings of the site
	 */
	public void setAvgLatency(double siteAvgLatency)
	{
		avgLatency = siteAvgLatency;
	}

	/**
	 * Sets the standard deviation of the average latency of the site.
	 * @param siteLatencyStdDeviation     Standard deviation for the average latency
	 */
	public void setLatencyStdDeviation(double siteLatencyStdDeviation)
	{
		latencyStdDeviation = siteLatencyStdDeviation;
	}

	/**
	 * Sets the deviation tolerance of the site.
	 * @param siteDeviationTolerance     Number of standard deviations before a ping is considered slow
	 */
	public void setDeviationTolerance(int siteDeviationTolerance)
	{
		deviationTolerance = siteDeviationTolerance;
	}
}
