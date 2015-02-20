import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.my.libraries.MoreDateFunctions;
import org.my.libraries.MoreMath;

public class PingThread extends Thread
{
	//Variables for ping command
	private String pingSite, command;
	private int msPerPing = 1000;
	
	//Variables for reporting
	private String dailyReport;
	private double averageLatency, latencyStdDev, maxLatency;
	private int deviationTolerance, reportHour, numOutages;
	private int outageEmergency = 30; //Seconds of outage before emergency contact
	private boolean wasSiteUnreachable; //Flag for whether the last ping was unreachable
	private boolean wasEmergencyNotified; //Flag for whether contact was already done
	private Date outageStart;
	private FileWriter logWriter;
	
	//Variables for recalculating standard deviation
	private ArrayList<Double> latencyValues;
	private Date lastRecalculated;
	private int hoursForUpdate = 6;
	private int sampleSize = 4000;
	
	//Consructor
	PingThread(String site, double avrgLatency, double stdDev, int tolerance)
	{
		pingSite = site;
		averageLatency = avrgLatency;
		latencyStdDev = stdDev;
		deviationTolerance = tolerance;
		wasSiteUnreachable = false;
		wasEmergencyNotified = false;
		dailyReport = "";
		latencyValues = new ArrayList<Double>(sampleSize);
		
		//Set command to perform single ping -c 1 works with Linux/Mac
		command = "ping -c 1 " + site;
		
		lastRecalculated = new Date();
		
		File logsDir = new File("logs");
		logsDir.mkdir();
		File logFile = new File(logsDir, site + "_log.txt");
		
		try
		{
			logWriter = new FileWriter(logFile, true);
		}
		catch(Exception e)
		{
			e.printStackTrace(System.err);
			System.exit(-1);
		}
	}
	
	public String getPingSite()
	{
		return pingSite;
	}

	public double getAverageLatency()
	{
		return averageLatency;
	}

	public double getLatencyStdDev()
	{
		return latencyStdDev;
	}

	public int getDeviationTolerance()
	{
		return deviationTolerance;
	}

	public Date getLastRecalculated()
	{
		return lastRecalculated;
	}

	public int getHoursForUpdate()
	{
		return hoursForUpdate;
	}

	public int getSampleSize()
	{
		return sampleSize;
	}
	
	public String getDailyReport()
	{
		return dailyReport;
	}
	
	public double getMaxLatency()
	{
		return maxLatency;
	}

	public int getNumOutages()
	{
		return numOutages;
	}

	public Date getOutageStart()
	{
		return outageStart;
	}

	public int getReportHour()
	{
		return reportHour;
	}

	public int getOutageEmergency()
	{
		return outageEmergency;
	}

	public boolean getWasSiteUnreachable()
	{
		return wasSiteUnreachable;
	}
	
	public boolean getWasEmergencyNotified()
	{
		return wasEmergencyNotified;
	}
	
	public void setPingSite(String site)
	{
		pingSite = site;
	}

	public void setAverageLatency(double avrgLatency)
	{
		averageLatency = avrgLatency;
	}

	public void setLatencyStdDev(double stdDev)
	{
		latencyStdDev = stdDev;
	}

	public void setDeviationTolerance(int tolerance)
	{
		deviationTolerance = tolerance;
	}
	
	public void setLastRecalculated(Date recalculatedDate)
	{
		lastRecalculated = recalculatedDate;
	}

	public void setHoursForUpdate(int hours)
	{
		hoursForUpdate = hours;
	}

	public void setSampleSize(int smplSize)
	{
		sampleSize = smplSize;
	}
	
	public void setDailyReport(String message)
	{
		dailyReport = message;
	}
	
	public void setMaxLatency(double max)
	{
		maxLatency = max;
	}

	public void setNumOutages(int numberOutages)
	{
		numOutages = numberOutages;
	}

	public void setOutageStart(Date start)
	{
		outageStart = start;
	}

	public void setReportHour(int hour)
	{
		reportHour = hour;
	}

	public void setOutageEmergency(int emergencyLimit)
	{
		outageEmergency = emergencyLimit;
	}

	public void setWasSiteUnreachable(boolean unreachable)
	{
		wasSiteUnreachable = unreachable;
	}
	
	public void setWasEmergencyNotified(boolean contacted)
	{
		wasEmergencyNotified = contacted;
	}
	
	public void appendDailyReport(String message)
	{
		if(message.isEmpty())
			setDailyReport("");
		else
			setDailyReport(dailyReport + "\n" + message);
	}

	@Override
	public void run()
	{
		while(true)
		{
			try
			{
				if(pingSite())
					sleep(msPerPing);
			}
			catch(Exception e)
			{
				e.printStackTrace(System.err);
			}
		}
	}

	private boolean pingSite() throws IOException, InterruptedException
	{
		String processInput = null;
		double pingLatency = -1;
		String pingReturnSite = "";
		
		Process pingProcess = java.lang.Runtime.getRuntime().exec(command); //Forks process and executes command
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(pingProcess.getInputStream()));
		int returnVal = pingProcess.waitFor(); //Waits for return from ping command
		boolean reachable = (returnVal == 0);
		
		int count = 0;
		while(((processInput = stdInput.readLine()) != null))
	    {
			if(count == 0)
			{
				//IP address taken from first line in stream
				pingReturnSite = parsePingIP(processInput, reachable);
			}
			else if(reachable && count == 1)
			{
				//Latency value taken from first line in stream
				pingLatency = parsePingLatency(processInput);
			}
			count++;
	    }
		handlePing(reachable, pingReturnSite, pingLatency);
		return reachable;
	}
	
	private double parsePingLatency(String pingString)
	{
		double pingLatency = -1;
		int latencySubstrStart, latencySubstrEnd;
		String latencySubstr;
		
		latencySubstrStart = pingString.indexOf("time=") + 5;
		latencySubstrEnd = pingString.indexOf(" ms");
		if(latencySubstrStart > 4 || latencySubstrEnd > -1)
		{
			latencySubstr = pingString.substring(latencySubstrStart, latencySubstrEnd);
			pingLatency = Double.parseDouble(latencySubstr);
		}
		
		return pingLatency;
	}
	
	private String parsePingIP(String pingString, boolean reachable)
	{
		String pingIP = pingString;
		int ipSubstrStart, ipSubstrEnd;
		
		ipSubstrStart = pingString.indexOf("(") + 1;
		ipSubstrEnd = pingString.indexOf(")");
		if(ipSubstrStart > 0 || ipSubstrEnd > -1)
			pingIP = pingString.substring(ipSubstrStart, ipSubstrEnd);
		
		return pingIP;
	}

	private void handlePing(boolean reachable, String pingIP, double pingLatency) throws IOException
	{
		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
		
		if(reachable)
		{
			if(getWasSiteUnreachable())
			{
				String message = dateFormat.format(currentDate) + ": " + getPingSite() + " (" + pingIP + ") is now reachable";
				writeToLog(message, "\n");
				if(getWasEmergencyNotified())
				{
					System.out.println("Emergency over in " + getPingSite() + " at " + dateFormat.format(currentDate));
					//E-mail report
				}
				resetEmergencyReporting();
			}
			if(pingLatency > (getAverageLatency() + (getLatencyStdDev() * getDeviationTolerance())))
			{
				String message = dateFormat.format(currentDate) + ": " + getPingSite() + " (" + pingIP + ") is reachable but slow with " + Double.toString(pingLatency) + " ms latency"; 
				writeToLog(message, "\n");
			}
			
			if(MoreMath.modulo((int)MoreDateFunctions.timeDiffInHours(currentDate, getLastRecalculated()), 24) >= getHoursForUpdate() )
			{
				latencyValues.add(pingLatency);
				if(latencyValues.size() >= getSampleSize())
				{
					setAverageLatency(MoreMath.mean(latencyValues));
					setLatencyStdDev(MoreMath.stdDev(latencyValues));
					setLastRecalculated(currentDate);
					latencyValues.clear();
					String message = dateFormat.format(currentDate) + ": " + getPingSite() + " - " + "Average Latency is adjusted to " + Double.toString(getAverageLatency()) +
							" and Standard Deviation is adjusted to " + Double.toString(getLatencyStdDev());
					writeToLog(message, "\n");
				}
			}
		}
		else
		{
			String message = dateFormat.format(currentDate) + ": " + getPingSite() + " (" + pingIP + ") is not reachable";
			writeToLog(message, "\n");
			if(getWasSiteUnreachable())
			{
				if((MoreDateFunctions.timeDiffInSeconds(currentDate, getOutageStart()) > getOutageEmergency()) && !getWasEmergencyNotified())
				{
					System.out.println("Emergency in " + getPingSite() + " at " + dateFormat.format(currentDate));
					setWasEmergencyNotified(true);
				}
			}
			else
			{
				setWasSiteUnreachable(true);
				setOutageStart(currentDate);
				//Increment numTimesDown for hour
			}
		}
	}
	
	private void resetEmergencyReporting()
	{
		setWasSiteUnreachable(false);
		setWasEmergencyNotified(false);
	}
	
	private void writeToLog(String message, String seperator) throws IOException
	{
		logWriter.write(message + seperator);
		logWriter.flush();
	}
}
