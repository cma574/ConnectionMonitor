import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import org.my.libraries.IOUtilities;

/**
 * The main application area. This will create the objects and launch the threads needed for 
 * ConnectionMonitor to run.
 * @author Cory Ma
 */
public class MonitorApp
{
	/**
	 * Launch point for the application.
	 * @param args     Command line arguments to program
	 */
	public static void main(String[] args)
    {
		ArrayList<ShutDownableThread> threadList = new ArrayList<>();
		Vector<String> activePingSiteNames = new Vector<String>(); //Tracks all active ping sites for querying
		
		DBAccessHandler dbAccessHandler;
		EmailReportHandler emailReportHandler;
		
		DBEmailerConfigManager dbEmailerConfigManager = new DBEmailerConfigManager();
		PingSitesConfigManager pingSitesConfigManager = new PingSitesConfigManager();
		
		boolean isShutDown = false;
		
		try
		{
			//Set up config managers.
			dbEmailerConfigManager.importConfig();
			pingSitesConfigManager.importConfig();
			
			ArrayList<PingSite> pingSites = pingSitesConfigManager.getPingSitesFromConfig();
			dbAccessHandler = dbEmailerConfigManager.buildDBAccessHandlerFromConfig();
			emailReportHandler = dbEmailerConfigManager.buildEmailReportHandlerFromConfig();

			dbAccessHandler.initDBConnection();
			
			RegularReportThread regularReportThread = new RegularReportThread(activePingSiteNames, emailReportHandler, dbAccessHandler);
			threadList.add(regularReportThread);
			
			for(PingSite pingSite : pingSites)
			{
				threadList.add(new PingThread(pingSite, new PingHandler(pingSite), emailReportHandler, dbAccessHandler));
				dbAccessHandler.insertSiteEntry(pingSite.getName(), pingSite.getAddress());
				activePingSiteNames.add(pingSite.getName());
			}
			
			startThreads(threadList);
			
			BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
			
			while(!isShutDown && threadList.size() > 1)
			{
				isShutDown = promptShutDownInput(consoleIn);
				if(isShutDown)
				{
					shutDownThreads(threadList);
				}
			}
			dbAccessHandler.closeDBConnection();
			IOUtilities.closeCloseable(consoleIn);
		}
		catch(ClassNotFoundException | SQLException e)
		{
			e.printStackTrace();
		}
    }
	
	/**
	 * Runs the start method on each ShutDownableThread in a list.
	 * @param threadList     List to iterate through
	 */
	private static void startThreads(ArrayList<ShutDownableThread> threadList)
	{
		for(Thread thread : threadList)
		{
			thread.start();
		}
	}
	
	/**
	 * Prompts user for shut down command of Q or q and loops until it receives it.
	 * @return true if the application should start shutting down
	 */
	private static boolean promptShutDownInput(BufferedReader consoleIn)
	{
		String consoleInput;
		boolean isShutDown = false;
		
		try
		{
			System.out.println("Please enter Q/q to terminate.");
			consoleInput = consoleIn.readLine();
			if(consoleInput.equals("q") || consoleInput.equals("Q"))
			{
				isShutDown = true;
			}
		}
		catch(IOException ioEx)
		{
			ioEx.printStackTrace();
		}
		
		return isShutDown;
	}
	
	/**
	 * Shuts down each ShutDownableThread in a list, then joins them to insure that the program ends gracefully.
	 * @param threadList     List of ShutDownableThreads to shutdown
	 */
	private static void shutDownThreads(ArrayList<ShutDownableThread> threadList)
	{
		for(ShutDownableThread thread : threadList)
		{
			thread.shutDown();
			try
			{
				thread.join();
			}
			catch(InterruptedException iE)
			{
				iE.printStackTrace();
			}
		}
	}
}
