package org.connectionmonitor.ping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for executing the ping command and parsing its results.
 * @author Cory Ma
 */
public class PingAction
{
	/**
	 * Executes the ping command and parses the results.
	 * @return true if the site was reachable
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws SQLException
	 */
	public static PingResponse pingSite(String siteAddress) throws IOException, InterruptedException
	{
		String processInput = null;
		String pingIP = "";
		double pingLatency = -1;
		String command = "ping -c 1 " + siteAddress;
		
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
	private static String parsePingIP(String pingString)
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
	private static double parsePingLatency(String pingString)
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
}
