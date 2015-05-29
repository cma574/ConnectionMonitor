import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Thread that runs the system's ping command, then parses and passes the results to
 * PingHandler for processing.
 * @author Cory Ma
 */
public class PingThread extends Thread
{
	//Variables for ping command
	private String command;
	private final int msPerPing = 2000;
	
	private PingSite pingSite;
	private PingHandler pingHandler;
	
	private boolean isShutDown = false;
	
	/**
	 * Constructor.
	 * @param site        PingSite to perform pings on
	 * @param handler     PingHandler to process results of ping
	 */
	public PingThread(PingSite site, PingHandler handler)
	{
		pingSite = site;
		
		//Set command to perform single ping -c 1 works with Linux/Mac
		command = "ping -c 1 " + pingSite.getAddress();
		pingHandler = handler;
	}
	
	/**
	 * Overridden Thread run() method. This will loop until the isShutDown flag is set, pinging repeatedly. For
	 * each successful ping it will sleep for time equal to msPerPing milliseconds. This is done to limit the checking
	 * somewhat. Unsuccessful pinging takes longer for the system command to return so a delay isn't necessary.
	 */
	@Override
	public void run()
	{
		while(!isShutDown)
		{
			try
			{
				if(pingSite())
					sleep(msPerPing);
			}
			catch(IOException | InterruptedException | SQLException e)
			{
				e.printStackTrace();
			}
		}
		pingHandler.closeFileWriter();
	}
	
	/**
	 * Sets the isShutDown flag to insure the thread shuts down cleanly.
	 */
	public void shutDown()
	{
		isShutDown = true;
	}

	/**
	 * Executes the ping command, parses the results, then passes them to the PingHandler.
	 * @return true if the site was reachable
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws SQLException
	 */
	private boolean pingSite() throws IOException, InterruptedException, SQLException
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
		pingHandler.handlePing(reachable, pingIP, pingLatency);
		return reachable;
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
}
