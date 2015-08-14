import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.my.libraries.MoreDateFunctions;
import org.my.libraries.MoreMath;

/**
 * Thread that runs the system's ping command, then parses and passes the results to
 * PingHandler for processing.
 * @author Cory Ma
 */
public class PingThread extends ShutDownableThread
{
	//Variables for ping command
	private String command;
	private final int MS_PER_PING = 1000;
	
	private PingSite pingSite;
	private PingHandler pingHandler;
	private EmailReportHandler emailReportHandler;
	private DBAccessHandler dbAccessHandler;
	
	//Variables for recalculating average latency
	private ArrayList<Double> latencyValues;
	private Date lastRecalculated;
	private int hoursForUpdate = 5;
	private int sampleSize = 4000;
	
	/**
	 * Constructor.
	 * @param site             PingSite to perform pings on
	 * @param handler          PingHandler to process results of ping
	 * @param emailHandler     EmailReportHandler for email report synchronization
	 * @param dbHandler        DBAccessHandler to handle read and write to database
	 */
	public PingThread(PingSite site, PingHandler handler, EmailReportHandler emailHandler, DBAccessHandler dbHandler)
	{
		super(site.getName() + "-PingThread");
		pingSite = site;
		emailReportHandler = emailHandler;
		dbAccessHandler = dbHandler;
		
		latencyValues = new ArrayList<Double>(sampleSize);
		lastRecalculated = new Date();
		
		//Set command to perform single ping -c 1 works with Linux/Mac
		command = "ping -c 1 " + pingSite.getAddress();
		pingHandler = handler;
	}
	
	/**
	 * Overridden Thread run() method. This will loop until the isShutDown flag is set, pinging repeatedly. For
	 * each successful ping it will sleep for time equal to MS_PER_PING milliseconds. This is done to limit the checking
	 * somewhat. Unsuccessful pinging takes longer for the system command to return so a delay isn't necessary.
	 */
	@Override
	public void run()
	{
		while(!isShutDown)
		{
			try
			{
				PingResponse pingResponse = pingSite();
				PingHandlerResponse handlerResponse = pingHandler.handlePing(pingResponse);
				handleResponse(pingResponse, handlerResponse);
				if(pingResponse.getReachable() && checkNeedPingSiteUpdate())
				{
					handlePingSiteUpdate(pingResponse.getLatency());
				}
				sleep(MS_PER_PING);
			}
			catch(IOException | InterruptedException | SQLException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Executes the ping command, parses the results, then passes them to the PingHandler.
	 * @return true if the site was reachable
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws SQLException
	 */
	private PingResponse pingSite() throws IOException, InterruptedException
	{
		String processInput = null;
		String pingIP = "";
		double pingLatency = -1;
		
        Process pingProcess = java.lang.Runtime.getRuntime().exec(command); //Forks process and executes command
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(pingProcess.getInputStream()));
		int returnVal = pingProcess.waitFor(); //Waits for return from ping command
		boolean reachable = (returnVal == 0);
		while(((processInput = stdInput.readLine()) != null))
		{
			if(pingIP.isEmpty())
			{
				pingIP = parsePingIP(processInput);
			}
			if(reachable && pingLatency == -1)
			{
				pingLatency = parsePingLatency(processInput);
			}
	    }
		return new PingResponse(reachable, pingIP, pingLatency);
	}
	
	/**
	 * Parses the IP address from output line from the ping.
	 * @param pingString     Output line from the ping
	 * @return The IP address if successful, otherwise an empty String
	 */
	private String parsePingIP(String pingString)
	{
		String pingIP = "";
		Pattern ipPattern = Pattern.compile(".*\\b(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\b.*");
		Matcher ipMatcher = ipPattern.matcher(pingString);
		if(ipMatcher.find())
		{
			pingIP = ipMatcher.group(1);
		}
		return pingIP;
	}
	
	/**
	 * Parses the latency from an output line from the ping.
	 * @param pingString     Output line from the ping
	 * @return The latency if successful, otherwise -1
	 */
	private double parsePingLatency(String pingString)
	{
		double pingLatency = -1;
		Pattern latencyPattern = Pattern.compile(".*time=(\\d{1,6}.\\d{1,3}) ms");
		Matcher latencyMatcher = latencyPattern.matcher(pingString);
		String latencyString;
		
		if(latencyMatcher.find())
		{
			latencyString = latencyMatcher.group(1);
			pingLatency = Double.parseDouble(latencyString);
		}
		
		return pingLatency;
	}

	/**
	 * Handles database access and site unreachable email sending based off of the results of a ping.
	 * @param pingResponse
	 * @param handlerResponse
	 * @throws SQLException
	 */
	private void handleResponse(PingResponse pingResponse, PingHandlerResponse handlerResponse) throws SQLException
	{
		String pingIP = pingResponse.getIP();
		double pingLatency = pingResponse.getLatency();
		Date currentDate = pingResponse.getPingTime();
		switch(handlerResponse)
		{
		case SITE_UNREACHABLE:
			emailReportHandler.incrementNumEmergencyReportSites(currentDate);
			dbAccessHandler.insertLogEntry(pingSite.getName(), 1, currentDate, pingIP, pingLatency);
			break;
		case SITE_REACHABLE_AGAIN:
			dbAccessHandler.insertLogEntry(pingSite.getName(), 2, currentDate, pingIP, pingLatency);
			CheckNeedEmailResponse needEmail = emailReportHandler.decrementNumEmergencyReportSites(currentDate);
			if(needEmail.getShouldSend())
			{
				String message = dbAccessHandler.buildEmergencyReportMessage(needEmail.getStartDateQuery(), needEmail.getEndDateQuery());
				emailReportHandler.sendReport(true, message);
			}
			break;
		case SITE_LATENCY_SLOW:
			dbAccessHandler.insertLogEntry(pingSite.getName(), 3, currentDate, pingIP, pingLatency);
			break;
		default:
		}
	}
	
	/**
	 * Checks to see if the associated PingSite needs to update latency values.
	 * @return true if necessary, false if not
	 */
	private boolean checkNeedPingSiteUpdate()
	{
		Date currentDate = new Date();
		return (MoreMath.modulo((int)MoreDateFunctions.timeDiffInHours(currentDate, lastRecalculated), 24) >= hoursForUpdate);
	}
	
	/**
	 * Adds the latency of a ping to a list, then calculates mean, and standard deviation and updates PingSite when enough data is collected.
	 * @param pingLatency     Latency of a ping
	 */
	private void handlePingSiteUpdate(Double pingLatency)
	{
		latencyValues.add(pingLatency);
		if(latencyValues.size() >= sampleSize)
		{
			lastRecalculated = MoreDateFunctions.roundToHour(new Date());
			pingSite.setAvgLatency(MoreMath.mean(latencyValues));
			pingSite.setLatencyStdDeviation(MoreMath.stdDev(latencyValues));
			latencyValues.clear();
		}
	}
}
