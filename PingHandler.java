import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.my.libraries.IOUtilities;
import org.my.libraries.MoreDateFunctions;
import org.my.libraries.MoreMath;

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
	private FileWriter logWriter;
	
	//Variables for recalculating standard deviation
	private ArrayList<Double> latencyValues;
	private Date lastRecalculated;
	private int hoursForUpdate = 5;
	private int sampleSize = 4000;
	
	/**
	 * Constructor.
	 * @param site             PingSite the object is handling results for
	 */
	public PingHandler(PingSite site)
	{
		pingSite = site;
		
		wasSiteUnreachable = false;
		needEmergencyNotification = false;
		latencyValues = new ArrayList<Double>(sampleSize);
		
		lastRecalculated = new Date();
		
		File logsDir = new File("logs");
		logsDir.mkdir();
		File logFile = new File(logsDir, site.getName() + "_log.txt");
		
		try
		{
			logWriter = new FileWriter(logFile, true);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * Determines what actions need to be done based on results of a ping.
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
			
			if(MoreMath.modulo((int)MoreDateFunctions.timeDiffInHours(currentDate, lastRecalculated), 24) >= hoursForUpdate )
			{
				latencyValues.add(pingLatency);
				if(latencyValues.size() >= sampleSize)
				{
					updatePingSiteInfo(currentDate);
				}
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
	 * Closes the FileWriter for the log.
	 */
	public void closeFileWriter()
	{
		IOUtilities.closeCloseable(logWriter);
	}
	
	/**
	 * Updates information for average latency and standard deviation for a PingSite.
	 * @param currentDate     Date the update happened for logging
	 */
	private void updatePingSiteInfo(Date currentDate)
	{
		pingSite.setAvgLatency(MoreMath.mean(latencyValues));
		pingSite.setLatencyStdDeviation(MoreMath.stdDev(latencyValues));
		lastRecalculated = currentDate;
		latencyValues.clear();
		String message = MoreDateFunctions.formatDateAsTimestamp(currentDate) + ": " + pingSite.getAddress() + " - " + "Average Latency is adjusted to " + 
				Double.toString(pingSite.getAvgLatency()) + " and Standard Deviation is adjusted to " + Double.toString(pingSite.getLatencyStdDeviation());
		writeToLog(message, "\n");
	}
	
	/**
	 * Resets flags set for emergency reporting.
	 */
	private void resetEmergencyReporting()
	{
		wasSiteUnreachable = false;
		needEmergencyNotification = false;
	}
	
	/**
	 * Writes a message to the log.
	 * @param message       Message to write
	 * @param separator     Separator to use between messages
	 */
	private void writeToLog(String message, String separator)
	{
		try
		{
			logWriter.write(message + separator);
			logWriter.flush();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
