import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

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
	private EmailReportHandler emailReportHandler;
	private DBAccessHandler dbAccessHandler;
	
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
	 * @param emailHandler     EmailReportHandler to synchronize sending of reports
	 * @param dbHandler        DBAccessHandler used for inserting log results
	 */
	public PingHandler(PingSite site, EmailReportHandler emailHandler, DBAccessHandler dbHandler)
	{
		pingSite = site;
		emailReportHandler = emailHandler; 
		dbAccessHandler = dbHandler;
		
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
	 * @param reachable       Whether the site was reachable
	 * @param pingIP          IP parsed out from the ping
	 * @param pingLatency     Latency of the ping
	 * @throws IOException
	 * @throws SQLException
	 */
	public void handlePing(boolean reachable, String pingIP, double pingLatency) throws IOException, SQLException
	{
		Date currentDate = new Date();
		emailReportHandler.checkReportSend();
		
		if(reachable)
		{
			if(wasSiteUnreachable)
			{
				String message = MoreDateFunctions.formatDateAsTimestamp(currentDate) + ": " + pingSite.getAddress() + " (" + pingIP + ") is now reachable";
				writeToLog(message, "\n");
				if(needEmergencyNotification)
				{
					dbAccessHandler.insertLogEntry(pingSite.getName(), 2, currentDate, pingIP, pingLatency);
					emailReportHandler.decrementNumEmergencyReportSites(currentDate);
				}
				resetEmergencyReporting();
			}
			if(pingLatency > (pingSite.getAvgLatency() + (pingSite.getLatencyStdDeviation() * pingSite.getDeviationTolerance())))
			{
				String message = MoreDateFunctions.formatDateAsTimestamp(currentDate) + ": " + pingSite.getAddress() + " (" + pingIP + ") is reachable but slow with " + Double.toString(pingLatency) + " ms latency"; 
				writeToLog(message, "\n");
				dbAccessHandler.insertLogEntry(pingSite.getName(), 3, currentDate, pingIP, pingLatency);
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
			String message = MoreDateFunctions.formatDateAsTimestamp(currentDate) + ": " + pingSite.getAddress() + " (" + pingIP + ") is not reachable";
			writeToLog(message, "\n");
			if(wasSiteUnreachable)
			{
				if((MoreDateFunctions.timeDiffInSeconds(currentDate, outageStart) > outageEmergency) && !needEmergencyNotification)
				{
					dbAccessHandler.insertLogEntry(pingSite.getName(), 1, currentDate, pingIP, pingLatency);
					needEmergencyNotification = true;
					emailReportHandler.incrementNumEmergencyReportSites(currentDate);
				}
			}
			else
			{
				wasSiteUnreachable = true;
				outageStart = currentDate;
			}
		}
	}
	
	/**
	 * Closes the FileWriter for the log.
	 */
	public void closeFileWriter()
	{
		try
		{
			logWriter.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Updates information for average latency and standard deviation for a PingSite.
	 * @param currentDate     Date the update happened for logging
	 * @throws IOException
	 */
	private void updatePingSiteInfo(Date currentDate) throws IOException
	{
		pingSite.setAvgLatency(MoreMath.mean(latencyValues));
		pingSite.setLatencyStdDeviation(MoreMath.stdDev(latencyValues));
		lastRecalculated = currentDate;
		latencyValues.clear();
		String message = MoreDateFunctions.formatDateAsTimestamp(currentDate) + ": " + pingSite.getAddress() + " - " + "Average Latency is adjusted to " + Double.toString(pingSite.getAvgLatency()) +
				" and Standard Deviation is adjusted to " + Double.toString(pingSite.getLatencyStdDeviation());
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
	 * @throws IOException
	 */
	private void writeToLog(String message, String separator) throws IOException
	{
		logWriter.write(message + separator);
		logWriter.flush();
	}
}
