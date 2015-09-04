package org.connectionmonitor.email;
import java.util.Date;

/**
 * An object for holding information for whether an email needs to be sent and what information to query on.
 * @author Cory Ma
 */
public class CheckNeedEmailResponse
{
	private boolean shouldSend;
	private Date startDateQuery, endDateQuery;
	
	/**
	 * Constructor. Sets shouldSend to false by default.
	 */
	public CheckNeedEmailResponse()
	{
		shouldSend = false;
	}
	
	/**
	 * Constructor. Sets shouldSend to true.
	 * @param startDate     Time the first site first became unreachable.
	 * @param endDate       Time the last site unreachable became reachable again
	 */
	public CheckNeedEmailResponse(Date startDate, Date endDate)
	{
		shouldSend = true;
		startDateQuery = startDate;
		endDateQuery = endDate;
	}
	
	/**
	 * Gets whether a message should be sent or not.
	 * @return true if a message should be sent, false if not
	 */
	public boolean getShouldSend()
	{
		return shouldSend;
	}
	
	/**
	 * Gets the time for the query to start.
	 * @return The start Date for querying
	 */
	public Date getStartDateQuery()
	{
		return startDateQuery;
	}
	
	/**
	 * Gets the time for the query to end.
	 * @return The end Date for querying
	 */
	public Date getEndDateQuery()
	{
		return endDateQuery;
	}
}
