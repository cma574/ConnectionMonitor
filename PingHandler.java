import java.util.Date;

import org.my.libraries.MoreDateFunctions;

/**
 * An object that receives results of a ping and processes them accordingly, writing to the database
 * and triggering email reports when necessary.
 * @author Cory Ma
 */
public class PingHandler
{
	private PingSite pingSite;
	
	//Variables for reporting
	private int outageEmergency = 15; //Seconds of outage before emergency contact is necessary
	private boolean wasSiteUnreachable; //Flag for whether the last ping was unreachable
	private boolean needEmergencyNotification; //Flag for whether contact was already done
	private Date outageStart;
	
	/**
	 * Constructor.
	 * @param site             PingSite the object is handling results for
	 */
	public PingHandler(PingSite site)
	{
		pingSite = site;
		
		wasSiteUnreachable = false;
		needEmergencyNotification = false;
	}
	
	/**
	 * Determines what actions need to be done based on results of a ping and returns the decision.
	 * @param pingResponse       Results of the ping
	 * @return NO_PROBLEMS by default
	 * 	        SITE_UNREACHABLE if site is unreachable after outageEmergency seconds have passed with wasSiteUnreachable is set
	 * 	        SITE_REACHABLE_AGAIN if site is reachable after wasSiteUnreachable is set
	 * 	        SITE_LATENCY_SLOW if site is reachable but latency is greater than bounds set in config
	 */
	public PingHandlerResponse handlePing(PingResponse pingResponse)
	{
		PingHandlerResponse response = PingHandlerResponse.NO_PROBLEMS;
		
		boolean reachable = pingResponse.getReachable();
		double pingLatency = pingResponse.getLatency();
		Date currentDate = pingResponse.getPingTime();
		
		if(reachable)
		{
			if(wasSiteUnreachable)
			{
				if(needEmergencyNotification)
				{
					response = PingHandlerResponse.SITE_REACHABLE_AGAIN;
				}
				resetEmergencyReporting();
			}
			else if(pingLatency > (pingSite.getAvgLatency() + (pingSite.getLatencyStdDeviation() * pingSite.getDeviationTolerance())))
			{
				response = PingHandlerResponse.SITE_LATENCY_SLOW;
			}
		}
		else
		{
			if(wasSiteUnreachable)
			{
				if((MoreDateFunctions.timeDiffInSeconds(currentDate, outageStart) > outageEmergency) && !needEmergencyNotification)
				{
					response = PingHandlerResponse.SITE_UNREACHABLE;
					needEmergencyNotification = true;
				}
			}
			else
			{
				wasSiteUnreachable = true;
				outageStart = currentDate;
			}
		}
		
		return response;
	}
	
	/**
	 * Resets flags set for emergency reporting.
	 */
	private void resetEmergencyReporting()
	{
		wasSiteUnreachable = false;
		needEmergencyNotification = false;
	}
}
