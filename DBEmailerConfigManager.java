import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.my.libraries.IOUtilities;


/**
 * An object for managing reads and writes from and to the DBEmailer config file used in ConnectionMonitor.
 * @author Cory Ma
 */
public class DBEmailerConfigManager
{
	private final String DBEMAILER_FILENAME = "DBEmailer.properties";
	private final String PROP_DBNAME = "DBName";
	private final String PROP_DBUSER = "DBUser";
	private final String PROP_DBPWD = "DBPassword";
	private final String PROP_REPORTFREQUENCY = "ReportFrequency";
	private final String PROP_NOTIFYLIST = "NotifyList";
	private final String PROP_EMERGENCYNOTIFYLIST = "EmergencyNotifyList";
	private final String PROP_STATIONNAME = "StationName";
	private final String PROP_EMAILADDR = "EmailAddress";
	private final String PROP_EMAILPWD = "EmailPassword";
	
	private Properties dbEmailConfig = new Properties();
	
	private static EmailReportHandler emailReportHandler;
	private static DBAccessHandler dbAccessHandler;
	
	/**
	 * Sets the DBName property. exportConfig() must be called to save changes.
	 * @param dbName     Name of the database
	 */
	public void setConfigDBName(String dbName)
	{
		dbEmailConfig.setProperty(PROP_DBNAME, dbName);
	}
	
	/**
	 * Sets the DBUser property. exportConfig() must be called to save changes.
	 * @param dbUser     The user name for the database
	 */
	public void setConfigDBUser(String dbUser)
	{
		dbEmailConfig.setProperty(PROP_DBUSER, dbUser);
	}
	
	/**
	 * Sets the DBPassword property. exportConfig() must be called to save changes.
	 * @param dbPassword     The password to access the database
	 */
	public void setConfigDBPassword(String dbPassword)
	{
		dbEmailConfig.setProperty(PROP_DBPWD, dbPassword);
	}
	
	/**
	 * Sets the ReportFrequency property. exportConfig() must be called to save changes.
	 * @param reportFrequency     Number of hours between reports
	 */
	public void setConfigReportFrequency(String reportFrequency)
	{
		dbEmailConfig.setProperty(PROP_REPORTFREQUENCY, reportFrequency);
	}
	
	/**
	 * Sets the NotifyList property. exportConfig() must be called to save changes.
	 * @param notifyList     Email address(es) to send regular reports to, separated by commas
	 */
	public void setConfigNotifyList(String notifyList)
	{
		dbEmailConfig.setProperty(PROP_NOTIFYLIST, notifyList);
	}
	
	/**
	 * Sets the EmergencyNotifyList property. exportConfig() must be called to save changes.
	 * @param emergencyNotifyList     Email address(es) to send emergency reports to, separated by commas
	 */
	public void setConfigEmergencyNotifyList(String emergencyNotifyList)
	{
		dbEmailConfig.setProperty(PROP_EMERGENCYNOTIFYLIST, emergencyNotifyList);
	}
	
	/**
	 * Sets the StationName property. Optional. exportConfig() must be called to save changes.
	 * @param stationName     Name of the station, to be appended to email headers.
	 */
	public void setConfigStationName(String stationName)
	{
		dbEmailConfig.setProperty(PROP_STATIONNAME, stationName);
	}
	
	/**
	 * Sets the EmailAddress property. exportConfig() must be called to save changes.
	 * @param emailAddress     Email address to send reports from, must currently be a Gmail account
	 */
	public void setConfigEmailAddress(String emailAddress)
	{
		dbEmailConfig.setProperty(PROP_EMAILADDR, emailAddress);
	}
	
	/**
	 * Sets the EmailPassword property. exportConfig() must be called to save changes.
	 * @param emailPassword     The password for the email account
	 */
	public void setConfigEmailPassword(String emailPassword)
	{
		dbEmailConfig.setProperty(PROP_EMAILPWD, emailPassword);
	}
	
	/**
	 * Imports settings from the DBEmailer.properties file.
	 * @return true if settings were successfully imported, false if an exception was thrown or the file is empty
	 */
	public boolean importConfig()
	{
		boolean success = true;
		
		try
		{
			InputStream dbEmailPropInStream = new FileInputStream(DBEMAILER_FILENAME);
			try
			{
				dbEmailConfig.load(dbEmailPropInStream);
				if(dbEmailConfig.size() == 0)
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
				IOUtilities.closeCloseable(dbEmailPropInStream);
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
	 * Exports settings to the DBEmailer.properties file.
	 * @return true if settings were successfully exported, false if an exception was thrown
	 */
	public boolean exportConfig()
	{
		boolean success = true;
		try
		{
			OutputStream dbEmailPropOutStream = new FileOutputStream(DBEMAILER_FILENAME);
			try
			{
				dbEmailConfig.store(dbEmailPropOutStream, null);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				success = false;
			}
			finally
			{
				IOUtilities.closeCloseable(dbEmailPropOutStream);
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
	 * Constructs a DBAccessHandler object from imported settings.
	 * @return The constructed DBAccessHandler object
	 */
	public DBAccessHandler buildDBAccessHandlerFromConfig()
	{
		if(dbAccessHandler == null)
		{
			String dbName = dbEmailConfig.getProperty(PROP_DBNAME);
			String dbUser = dbEmailConfig.getProperty(PROP_DBUSER);
			String dbPwd = dbEmailConfig.getProperty(PROP_DBPWD);
			dbAccessHandler = new DBAccessHandler(dbName, dbUser, dbPwd);
		}
		
		return dbAccessHandler;
	}
	
	/**
	 * Constructs an EmailReportHandler from imported settings.
	 * @return The constructed EmailReportHandler object
	 */
	public EmailReportHandler buildEmailReportHandlerFromConfig()
	{
		if(emailReportHandler == null)
		{
			Emailer emailer = buildEmailerFromConfig();
			
			int reportFrequency = Integer.parseInt(dbEmailConfig.getProperty(PROP_REPORTFREQUENCY));
			String notifyList = dbEmailConfig.getProperty(PROP_NOTIFYLIST);
			String emergencyNotifyList = dbEmailConfig.getProperty(PROP_EMERGENCYNOTIFYLIST);
			String stationName = dbEmailConfig.getProperty(PROP_STATIONNAME);
			emailReportHandler = new EmailReportHandler(emailer, reportFrequency, notifyList, emergencyNotifyList, stationName);
		}
		
		return emailReportHandler;
	}
	
	
	/**
	 * Constructs an Emailer object from imported settings.
	 * @return The constructed Emailer object
	 */
	private Emailer buildEmailerFromConfig()
	{
		String emailAddress = dbEmailConfig.getProperty(PROP_EMAILADDR);
		String emailPwd = dbEmailConfig.getProperty(PROP_EMAILPWD);
		return new Emailer(emailAddress, emailPwd);
	}
}
