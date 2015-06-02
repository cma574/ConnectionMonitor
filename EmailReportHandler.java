import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.my.libraries.MoreDateFunctions;

/**
 * An object that handles access to Emailer and parses together information from DBAccessHandler to create reports.
 * Since multiple threads require access to the database concurrently, one object should be shared and functions
 * need to be threadsafe. 
 * @author Cory Ma
 */
public class EmailReportHandler
{
	private Emailer emailer;
	private DBAccessHandler dbAccessHandler;
	private String notifyList, emergencyNotifyList; 
	private int reportFrequency;
	private volatile int numEmergencyReportSites;
	private ArrayList<String> activePingSiteNames;
	private Date startEmergency, lastRegularReport;
	private String stationHeader;
	
	/**
	 * Constructor
	 * @param emailSender            Emailer for sending reports with
	 * @param dbHandler              DBAccessHandler for getting information to create reports
	 * @param emailFrequency         Number of hours between regular reports
	 * @param emailList              List of email addresses to send regular reports to
	 * @param emergencyEmailList     List of email addresses to send emergency reports to
	 * @param stationName            Name of the station, to add to email subjects
	 */
	public EmailReportHandler(Emailer emailSender, DBAccessHandler dbHandler, int emailFrequency, String emailList, String emergencyEmailList, String stationName)
	{
		emailer = emailSender;
		dbAccessHandler = dbHandler;
		notifyList = emailList;
		emergencyNotifyList = emergencyEmailList;
		reportFrequency = emailFrequency;
		numEmergencyReportSites = 0;
		activePingSiteNames = new ArrayList<>();
		lastRegularReport = MoreDateFunctions.roundToHour(new Date());
		if(!stationName.isEmpty())
			stationHeader = stationName + " - ";
		else
			stationHeader = "";
	}
	
	/**
	 * Adds a name associated with a PingSite to the list.
	 * @param siteName     Name associated with a PingSite
	 */
	public synchronized void addSiteNameToList(String siteName)
	{
		activePingSiteNames.add(siteName);
	}
	
	/**
	 * Checks to see if a regular report needs to be sent.
	 * @throws SQLException
	 */
	public synchronized void checkReportSend() throws SQLException
	{
		Date currentDate = new Date();
		if(MoreDateFunctions.timeDiffInHours(currentDate, lastRegularReport) >= reportFrequency)
			sendRegularReport(MoreDateFunctions.roundToHour(currentDate));
	}
	
	/**
	 * Increments the number of PingSites that are unreachable, notes the time when the first one becomes unreachable.
	 * @param emergencyStartTime     Time the site became unreachable
	 */
	public synchronized void incrementNumEmergencyReportSites(Date emergencyStartTime)
	{
		numEmergencyReportSites++;
		if(numEmergencyReportSites == 1)
			startEmergency = new Date(emergencyStartTime.getTime() - 5000); //Subtract 5 seconds to account for reporting discrepancies
	}
	
	/**
	 * Decrements the number of PingSites that are unreachable, sends the emergency report when all are reachable again.
	 * @param emergencyEndTime     Time the site became reachable again
	 * @throws SQLException
	 */
	public synchronized void decrementNumEmergencyReportSites(Date emergencyEndTime) throws SQLException
	{
		numEmergencyReportSites--;
		if(numEmergencyReportSites == 0)
			sendEmergencyReport(new Date(emergencyEndTime.getTime() + 5000)); //Add 5 seconds to account for reporting discrepancies
	}
	
	/**
	 * Queries the database and gets information to form and send the regular report email.
	 * @param currentDate     Current time to end report at
	 * @throws SQLException
	 */
	private synchronized void sendRegularReport(Date currentDate) throws SQLException
	{
		String subject = stationHeader + "ConnectionMonitor Regular Report";
		String message = "";
		for(String siteName : activePingSiteNames)
		{
			int unreachableCount = 0;
			double maxLatency = 0;
			ArrayList<SiteRecord> siteRecords = dbAccessHandler.selectReportLog(siteName, lastRegularReport, currentDate);
			for(SiteRecord siteRecord : siteRecords)
			{
				if(siteRecord.getStatusNum() == 1)
					unreachableCount++;
				else if(siteRecord.getStatusNum() == 3)
				{
					if(siteRecord.getLatency() > maxLatency)
						maxLatency = siteRecord.getLatency();
				}
			}
			if(unreachableCount != 0 || maxLatency != 0)
			{
				message += "Report for " + siteName + ": \nNumber of Times Unreachable: " + Integer.toString(unreachableCount) + 
						"\nMaximum Latency: " + Double.toString(maxLatency) + "\n\n";
			}
		}
		
		if(message.isEmpty())
			message = "Nothing to report.";
		
		message = "ConnectionMonitor Report for " + MoreDateFunctions.formatDateAsTimestamp(lastRegularReport) + " to " + 
				MoreDateFunctions.formatDateAsTimestamp(currentDate) + "\n\n" + message;
		
		emailer.sendMessage(notifyList, subject, message);
		lastRegularReport = currentDate;
	}
	
	/**
	 * Queries the database and gets information to form and send the emergency report email.
	 * @param emergencyEndTime     Time all sites became reachable to end report at
	 * @throws SQLException
	 */
	private synchronized void sendEmergencyReport(Date emergencyEndTime) throws SQLException
	{
		String subject = stationHeader + "ConnectionMonitor Site Unreachable Notification";
		ArrayList<SiteRecord> siteRecords = dbAccessHandler.selectEmergencyReportLog(startEmergency, emergencyEndTime);
		String message = "";
		for(SiteRecord siteRecord : siteRecords)
		{
			if(siteRecord.getStatusNum() == 1)
				message += MoreDateFunctions.formatDateAsTimestamp(new Date(siteRecord.getPingTime().getTime())) + ": " + 
						siteRecord.getSiteName() + " became unreachable.\n";
			else if(siteRecord.getStatusNum() == 2)
				message += MoreDateFunctions.formatDateAsTimestamp(new Date(siteRecord.getPingTime().getTime())) + ": " + 
						siteRecord.getSiteName() + " became reachable again.\n";
		}
		
		emailer.sendMessage(emergencyNotifyList, subject, message);
	}
}