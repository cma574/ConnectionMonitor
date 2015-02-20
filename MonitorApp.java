import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.json.JSONObject;


public class MonitorApp
{
	public static void main(String[] args)
    {
		Properties config = new Properties();
		InputStream input = null;
		JSONObject configJSON;
		
		try
		{
			input = new FileInputStream("PingSites.properties");
	 
			//Load a properties file from input
			config.load(input);
	 
			Enumeration<?> configEnum = config.propertyNames();
			while (configEnum.hasMoreElements())
			{
				//Gets key from enumeration from properties file
				String key = (String) configEnum.nextElement();
				configJSON = new JSONObject(config.getProperty(key));
				String configSite = configJSON.getString("site");
				System.out.println(configSite);
				double configAverage = configJSON.getDouble("average");
				System.out.println(configAverage);
				double configStdDev = configJSON.getDouble("stddev"); 
				System.out.println(configStdDev);
				int configTolerance = configJSON.getInt("tolerance");
				System.out.println(configTolerance);
				//If there was an error in any of these JSON requests an exception will be thrown before it hits the constructor
				new PingThread(configSite, configAverage, configStdDev, configTolerance).start();
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			if (input != null)
			{
				try
				{
					input.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
    }
}
