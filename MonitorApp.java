import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The main application area. This will create the objects and launch the threads needed for 
 * ConnectionMonitor to run.
 * @author Cory Ma
 */
public class MonitorApp
{
	/**
	 * Main area of application, gets information to create objects and launch threads.
	 * @param args     Command line arguments to program
	 */
	public static void main(String[] args)
    {
		ArrayList<PingThread> pingThreads = new ArrayList<>();
		Vector<String> activePingSiteNames = new Vector<String>(); //Tracks all active ping sites for querying

		DBAccessHandler dbAccessHandler;
		Emailer emailer;
		EmailReportHandler emailReportHandler;
		
		Properties pingSitesConfig = new Properties();
		Properties emailerConfig = new Properties();
		InputStream emailPropStream;
		InputStream pingPropStream;
		
		boolean isShutDown = false;
		
		try
		{
			emailPropStream = new FileInputStream("DBEmailer.properties");
			pingPropStream = new FileInputStream("PingSites.properties");
			
			try
			{
				//Load properties files from FileInputStreams
				emailerConfig.load(emailPropStream);
				pingSitesConfig.load(pingPropStream);
				//If anything before this point throws an IOException the next section will be skipped.
				
				String dbName = emailerConfig.getProperty("DBName");
				String dbUser = emailerConfig.getProperty("DBUser");
				String dbPwd = emailerConfig.getProperty("DBPassword");
				String emailAddress = emailerConfig.getProperty("EmailAddress");
				String emailPwd = emailerConfig.getProperty("EmailPassword");
				int reportFrequency = Integer.parseInt(emailerConfig.getProperty("ReportFrequency"));
				String notifyList = emailerConfig.getProperty("NotifyList");
				String emergencyNotifyList = emailerConfig.getProperty("EmergencyNotifyList");
				String stationName = emailerConfig.getProperty("StationName");
				
				dbAccessHandler = new DBAccessHandler(dbName, dbUser, dbPwd, activePingSiteNames);
				dbAccessHandler.initDBConnection();
				emailer = new Emailer(emailAddress, emailPwd);
				
				emailReportHandler = new EmailReportHandler(emailer, reportFrequency, notifyList, emergencyNotifyList, stationName);
				
				ArrayList<PingSite> pingSites = getPingSitesFromConfig(pingSitesConfig);
				
				for(PingSite pingSite : pingSites)
				{
					pingThreads.add(new PingThread(pingSite, new PingHandler(pingSite), emailReportHandler, dbAccessHandler));
					dbAccessHandler.insertSiteEntry(pingSite.getName(), pingSite.getAddress());
					activePingSiteNames.add(pingSite.getName());
				}
				
				startThreads(pingThreads);
				
				BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
				
				while(!isShutDown && pingThreads.size() > 0)
				{
					isShutDown = promptShutDownInput(consoleIn);
					if(isShutDown)
					{
						shutDownThreads(pingThreads);
					}
				}
				
				dbAccessHandler.closeDBConnection();
				consoleIn.close();
			}
			catch(IOException | ClassNotFoundException | SQLException e)
			{
				e.printStackTrace();
			}
			finally
			{
				//Closing InputStreams and DataBase Connection
				closeInputStream(pingPropStream);
				closeInputStream(emailPropStream);
			}
		}
		catch(FileNotFoundException e) //If these Exceptions are thrown then nothing was successfully opened
		{
			e.printStackTrace();
		}
    }
	
	/**
	 * Creates a list of PingSites using information parsed from a JSON format String stored in a config file.
	 * @param pingSitesConfig     Opened config containing information to create PingSites
	 * @return The list of PingSites created from the config file
	 */
	private static ArrayList<PingSite> getPingSitesFromConfig(Properties pingSitesConfig)
	{
		ArrayList<PingSite> pingSites = new ArrayList<>();
		Enumeration<?> pingSitesConfigEnum = pingSitesConfig.propertyNames();
		
		while(pingSitesConfigEnum.hasMoreElements())
		{
			try
			{
				//Gets key from enumeration from properties file
				String key = (String)pingSitesConfigEnum.nextElement();
				JSONObject pingSitesConfigJSON = new JSONObject(pingSitesConfig.getProperty(key));
				String configAddress = pingSitesConfigJSON.getString("address");
				System.out.println(configAddress);
				double configAverage = pingSitesConfigJSON.getDouble("average");
				System.out.println(configAverage);
				double configStdDev = pingSitesConfigJSON.getDouble("stddev"); 
				System.out.println(configStdDev);
				int configTolerance = pingSitesConfigJSON.getInt("tolerance");
				System.out.println(configTolerance);
				//If there was an error in the JSON request an exception will be thrown before it hits the constructors, then it will go for the next properties entry
				pingSites.add(new PingSite(key, configAddress, configAverage, configStdDev, configTolerance));
			}
			catch(JSONException jsonEx)
			{
				jsonEx.printStackTrace();
			}
		}
		
		return pingSites;
	}
	
	/**
	 * Runs the start method on each PingThread in a list.
	 * @param pingThreads     List to iterate through
	 */
	private static void startThreads(ArrayList<PingThread> pingThreads)
	{
		for(Thread pingThread : pingThreads)
		{
			pingThread.start();
		}
	}
	
	/**
	 * Prompts user for shut down command of Q or q and loops until it receives it.
	 * @return true if the application should start shutting down gracefully
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
	 * Shuts down each pingThread in a list, then joins them to insure that the program ends gracefully.
	 * @param pingThreads     List of pingThreads to shutdown
	 */
	private static void shutDownThreads(ArrayList<PingThread> pingThreads)
	{
		for(PingThread pingThread : pingThreads)
		{
			pingThread.shutDown();
			try
			{
				pingThread.join();
			}
			catch(InterruptedException iE)
			{
				iE.printStackTrace();
			}
		}
	}
	
	/**
	 * Closes an input stream.
	 * @param closingStream     InputStream to close
	 */
	private static void closeInputStream(InputStream closingStream)
	{
		try
		{
			closingStream.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
