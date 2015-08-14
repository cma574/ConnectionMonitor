import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;
import org.my.libraries.IOUtilities;

/**
 * An object for managing reads and writes from and to the PingSites config file used in ConnectionMonitor.
 * @author Cory Ma
 */
public class PingSitesConfigManager
{
	private final String PINGSITES_FILENAME = "PingSites.properties";
	private final String JSON_ADDR = "address";
	private final String JSON_AVG = "average";
	private final String JSON_STDDEV = "stddev";
	private final String JSON_TOLERANCE = "tolerance";
	
	private Properties pingSitesConfig = new Properties();
	
	/**
	 * Adds or modifies PingSite entry in config. exportConfig() must be called to save changes.
	 * @param pingSite     PingSite to add to the Properties file
	 */
	public void addPingSite(PingSite pingSite)
	{
		JSONObject pingSiteJSON = new JSONObject();
		pingSiteJSON.put(JSON_ADDR, pingSite.getAddress());
		pingSiteJSON.put(JSON_AVG, pingSite.getAvgLatency());
		pingSiteJSON.put(JSON_STDDEV, pingSite.getLatencyStdDeviation()); 
		pingSiteJSON.put(JSON_TOLERANCE, pingSite.getDeviationTolerance());
		pingSitesConfig.setProperty(pingSite.getName(), pingSiteJSON.toString());
	}
	
	/**
	 * Removes PingSite from config. exportConfig() must be called to save changes.
	 * @param siteName     Name of PingSite to remove
	 */
	public void removePingSite(String siteName)
	{
		pingSitesConfig.remove(siteName);
	}
	
	/**
	 * Checks whether a name is already used as a key in the config file.
	 * @param siteName     Name of the site to check
	 * @return true if the siteName is already used as a key in the config file, false if not
	 */
	public boolean isNameInConfig(String siteName)
	{
		boolean isInConfig = false;
		
		Enumeration<?> pingSitesConfigEnum = pingSitesConfig.propertyNames();
		while(isInConfig == false && pingSitesConfigEnum.hasMoreElements())
		{
			if(pingSitesConfigEnum.nextElement().toString().equals(siteName));
			{
				isInConfig = true;
			}
		}
		
		return isInConfig;
	}

	/**
	 * Imports settings from the PingSites.properties file.
	 * @return true if settings were successfully imported, false if an exception was thrown or the file is empty
	 */
	public boolean importConfig()
	{
		boolean success = true;
		
		try
		{
			InputStream pingPropInStream = new FileInputStream(PINGSITES_FILENAME);
			try
			{
				pingSitesConfig.load(pingPropInStream);
				if(pingSitesConfig.size() == 0)
				{
					success = false;
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
				success = false;
			}
			finally
			{
				IOUtilities.closeCloseable(pingPropInStream);
			}

		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
			success = false;
		}
		
		return success;
	}
	
	/**
	 * Exports settings to the PingSites.properties file.
	 * @return true if settings were successfully exported, false if an exception was thrown
	 */
	public boolean exportConfig()
	{
		boolean success = true;
		try
		{
			OutputStream pingPropOutStream = new FileOutputStream(PINGSITES_FILENAME);
			try
			{
				pingSitesConfig.store(pingPropOutStream, null);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				success = false;
			}
			finally
			{
				IOUtilities.closeCloseable(pingPropOutStream);
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			success = false;
		}
		
		return success;
	}
	
	/**
	 * Creates a list of PingSites using information parsed from a JSON format String stored in a config file.
	 * @return The list of PingSites created from the config file
	 */
	public ArrayList<PingSite> getPingSitesFromConfig()
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
				String configAddress = pingSitesConfigJSON.getString(JSON_ADDR);
				double configAverage = pingSitesConfigJSON.getDouble(JSON_AVG);
				double configStdDev = pingSitesConfigJSON.getDouble(JSON_STDDEV); 
				int configTolerance = pingSitesConfigJSON.getInt(JSON_TOLERANCE);
				//If there was an error in the JSON request an exception will be thrown before it hits the constructor, then it will go for the next properties entry
				PingSite newPingSite = new PingSite(key, configAddress, configAverage, configStdDev, configTolerance);
				System.out.println(newPingSite);
				pingSites.add(newPingSite);
			}
			catch(JSONException jsonEx)
			{
				jsonEx.printStackTrace();
			}
		}
		
		return pingSites;
	}
}
