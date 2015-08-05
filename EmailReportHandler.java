import java.util.Date;

import org.my.libraries.MoreDateFunctions;

/**
 * An object that handles synchronization for Emailer and parses together information from DBAccessHandler to create reports.
 * Since multiple threads require access to the database concurrently, one object should be shared and functions
 * need to be threadsafe. 
 * @author Cory Ma
 */
public class EmailReportHandler
{
	private final Emailer emailer;
	private String notifyList, emergencyNotifyList; 
	private int reportFrequency;
	private volatile int numEmergencyReportSites;
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
	public EmailReportHandler(Emailer emailSender, int emailFrequency, String emailList, String emergencyEmailList, String stationName)
	{
		emailer = emailSender;
		notifyList = emailList;
		emergencyNotifyList = emergencyEmailList;
		reportFrequency = emailFrequency;
		numEmergencyReportSites = 0;
		lastRegularReport = MoreDateFunctions.roundToHour(new Date());
		if(!stationName.isEmpty())
		{
			stationHeader = stationName + " - ";
		}
		else
		{
			stationHeader = "";
		}
	}
	
	/**
	 * Checks to see if a regular report needs to be sent.
	 */
	public synchronized CheckNeedEmailResponse checkReportSend()
	{
		CheckNeedEmailResponse response;
		Date currentDate = new Date();
		if(MoreDateFunctions.timeDiffInHours(currentDate, lastRegularReport) >= reportFrequency)
		{
			response = new CheckNeedEmailResponse(lastRegularReport, currentDate);
			lastRegularReport = MoreDateFunctions.roundToHour(currentDate);
		}
		else
		{
			response = new CheckNeedEmailResponse();
		}
		return response;
	}
	
	/**
	 * Increments the number of PingSites that are unreachable, notes the time when the first one becomes unreachable.
	 * @param emergencyStartTime     Time the site became unreachable
	 */
	public synchronized void incrementNumEmergencyReportSites(Date emergencyStartTime)
	{
		numEmergencyReportSites++;
		if(numEmergencyReportSites == 1)
		{
			startEmergency = new Date(emergencyStartTime.getTime() - 5000); //Subtract 5 seconds to account for reporting discrepancies
		}
	}
	
	/**
	 * Decrements the number of PingSites that are unreachable, sends the emergency report when all are reachable again.
	 * @param emergencyEndTime     Time the site became reachable again
	 */
	public synchronized CheckNeedEmailResponse decrementNumEmergencyReportSites(Date emergencyEndTime)
	{
		CheckNeedEmailResponse response;
		numEmergencyReportSites--;
		if(numEmergencyReportSites == 0)
		{
			response = new CheckNeedEmailResponse(startEmergency, new Date(emergencyEndTime.getTime() + 5000));
		}
		else
		{
			response = new CheckNeedEmailResponse();
		}
		return response;
	}
	
	/**
	 * Synchronizes sending of email.
	 * @param message     Time all sites became reachable to end report at
	 */
	public synchronized void sendReport(boolean isEmergency, String message)
	{
		if(isEmergency)
		{
			String subject = stationHeader + "ConnectionMonitor Site Unreachable Notification";
			emailer.sendMessage(emergencyNotifyList, subject, message);
		}
		else
		{
			String subject = stationHeader + "ConnectionMonitor Regular Report";
			emailer.sendMessage(notifyList, subject, message);
		}
	}
}