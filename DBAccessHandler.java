import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import org.my.libraries.MoreDateFunctions;

/**
 * An object that handles access to DataBase and performs necessary queries and input to it. Since multiple
 * threads require access to the database concurrently, one object should be shared and functions
 * need to be threadsafe. 
 * @author Cory Ma
 */
public class DBAccessHandler
{
	//JDBC Driver Name and Database URL
	private final String JDBC_DRIVER;  
	private final String DB_URL;
	
	//Database Credentials
	private final String USER;
	private final String PASSWORD;
	
	//Table Names
	private final String LOG_TABLE;
	private final String SITE_TABLE;
	
	private Connection dbConnection;
	
	/**
	 * Constructor.
	 * @param dbName         Name of the database
	 * @param dbUser         User name for database
	 * @param dbPassword     Password for database
	 */
	public DBAccessHandler(String dbName, String dbUser, String dbPassword)
	{
		DB_URL = "jdbc:mysql://localhost/" + dbName;
		USER = dbUser;
		PASSWORD = dbPassword;
		
		JDBC_DRIVER = "com.mysql.jdbc.Driver";
		LOG_TABLE = "log";
		SITE_TABLE = "site";
	}
	
	/**
	 * Initializes the DataBase connection by loading the driver and establishing the connection.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public synchronized void initDBConnection() throws ClassNotFoundException, SQLException
	{
		Class.forName(JDBC_DRIVER);
		dbConnection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
	}
	
	/**
	 * Closes the connection to the database, should not be called outside the main thread.
	 */
	public synchronized void closeDBConnection()
	{
		try
		{
			dbConnection.close();
		}
		catch(SQLException sqlE)
		{
			sqlE.printStackTrace();
		}
	}
	
	/**
	 * Inserts an entry into the log table in the database.
	 * @param siteName      Name assigned to the PingSite
	 * @param statusNum     Status number associated with the type of response to the ping
	 * 	        1 - Site unreachable
	 *          2 - Site reachable again
	 *          3 - Site Latency Slow
	 * @param pingTime      Time of the ping command
	 * @param ipAddress     IP Address logged from the ping
	 * @param latency       Latency logged from the ping
	 * @throws SQLException
	 */
	public synchronized void insertLogEntry(String siteName, int statusNum, Date pingTime, String ipAddress, double latency) throws SQLException
	{
		Statement sqlStatement = dbConnection.createStatement();
		
		int sitePKey = getSitePKey(siteName);
		
		Timestamp timestampInsert = new Timestamp(pingTime.getTime());; 
		String latencyInsertString = "NULL";
		String ipInsertString = "NULL";
		if(!ipAddress.isEmpty())
		{
			ipInsertString = ipAddress;
		}
		if(latency != -1)
		{
			latencyInsertString = Double.toString(latency);
		}
		
		String sql = "INSERT INTO " + LOG_TABLE + " (fksiteid, fkstatusid, pingtime, ipaddress, latency) VALUES (" + Integer.toString(sitePKey) + ", " + 
				Integer.toString(statusNum) + ", '" + timestampInsert + "', '" + ipInsertString + "', " + latencyInsertString + ")";

		sqlStatement.executeUpdate(sql);
		sqlStatement.close();
	}
	
	/**
	 * Inserts an entry into the site table with PingSite information.
	 * @param siteName     Name assigned to the PingSite
	 * @param address      Address assigned to the PingSite
	 * @throws SQLException
	 */
	public synchronized void insertSiteEntry(String siteName, String address) throws SQLException
	{
		int sitePKey = getSitePKey(siteName);
		
		if(sitePKey == -1)
		{
			Statement sqlStatement = dbConnection.createStatement();
			String sql = "INSERT INTO " + SITE_TABLE + " (name, address) VALUES ('" +
					siteName + "', '" + address + "')";
			
			sqlStatement.executeUpdate(sql);
			sqlStatement.close();
		}
	}
	
	/**
	 * Queries the database and builds a message string for a regular report email.
	 * @param activePingSiteNames     List of active ping site names
	 * @param emergencyStartTime      Time to start the report at
	 * @param emergencyEndTime        Time to end the report at
	 * @return The String for the regular report email
	 * @throws SQLException
	 */
	public synchronized String buildRegularReportMessage(Vector<String> activePingSiteNames, Date reportStartTime, Date reportEndTime) throws SQLException
	{
		String message = "";
		for(String siteName : activePingSiteNames)
		{
			int unreachableCount = 0;
			double maxLatency = 0;
			ArrayList<SiteRecord> siteRecords = selectReportLog(siteName, reportStartTime, reportEndTime);
			for(SiteRecord siteRecord : siteRecords)
			{
				if(siteRecord.getStatusNum() == 1)
				{
					unreachableCount++;
				}
				else if(siteRecord.getStatusNum() == 3)
				{
					if(siteRecord.getLatency() > maxLatency)
					{
						maxLatency = siteRecord.getLatency();
					}
				}
			}
			if(unreachableCount != 0 || maxLatency != 0)
			{
				message += "Report for " + siteName + ": \nNumber of Times Unreachable: " + Integer.toString(unreachableCount) + 
						"\nMaximum Latency: " + Double.toString(maxLatency) + "\n\n";
			}
		}
		
		if(message.isEmpty())
		{
			message = "Nothing to report.";
		}
		message = "ConnectionMonitor Report for " + MoreDateFunctions.formatDateAsTimestamp(reportStartTime) + " to " + 
				MoreDateFunctions.formatDateAsTimestamp(reportEndTime) + "\n\n" + message;
		
		return message;
	}
	
	/**
	 * Queries the database and builds a message string for an emergency report email.
	 * @param emergencyStartTIme     Time to start the report at
	 * @param emergencyEndTime       Time to end the report at
	 * @return The String for the emergency report email
	 * @throws SQLException
	 */
	public synchronized String buildEmergencyReportMessage(Date emergencyStartTime, Date emergencyEndTime) throws SQLException
	{
		ArrayList<SiteRecord> siteRecords = selectEmergencyReportLog(emergencyStartTime, emergencyEndTime);
		String message = "";
		for(SiteRecord siteRecord : siteRecords)
		{
			if(siteRecord.getStatusNum() == 1)
			{
				message += MoreDateFunctions.formatDateAsTimestamp(new Date(siteRecord.getPingTime().getTime())) + ": " + 
						siteRecord.getSiteName() + " became unreachable.\n";
			}
			else if(siteRecord.getStatusNum() == 2)
			{
				message += MoreDateFunctions.formatDateAsTimestamp(new Date(siteRecord.getPingTime().getTime())) + ": " + 
						siteRecord.getSiteName() + " became reachable again.\n";
			}
		}
		
		return message;
	}
	
	/**
	 * Performs a query on the log table to get information required for a regular report email.
	 * @param siteName      Name assigned to the PingSite
	 * @param startTime     Time to start the query's WHERE clause from
	 * @param endTime       Time to end the query's WHERE clause at
	 * @return The list for the EmailReportHandler to parse out information
	 * @throws SQLException
	 */
	private synchronized ArrayList<SiteRecord> selectReportLog(String siteName, Date startTime, Date endTime) throws SQLException
	{
		ArrayList<SiteRecord> siteRecords = new ArrayList<>();
		int sitePKey = getSitePKey(siteName);
		Timestamp startTimestamp = new Timestamp(startTime.getTime());
		Timestamp endTimestamp = new Timestamp(endTime.getTime());
		
		if(sitePKey != -1)
		{
			Statement sqlStatement = dbConnection.createStatement();
			String sql = "SELECT * FROM " + LOG_TABLE + " WHERE (pingtime >= '" + startTimestamp + "' AND pingtime < '" + endTimestamp + "') AND fksiteid = " + Integer.toString(sitePKey);
			
			ResultSet queryResult = sqlStatement.executeQuery(sql);

			while(queryResult.next())
			{
				siteRecords.add(new SiteRecord(siteName, queryResult.getInt("fkstatusid"), queryResult.getTimestamp("pingtime"), queryResult.getFloat("latency")));
			}

			sqlStatement.close();
		}
		
		return siteRecords;
	}
	
	/**
	 * Performs a query on the log table to get information required for an emergency report email.
	 * @param startTime     Time to start the query's WHERE clause from
	 * @param endTime       Time to end the query's WHERE clause at
	 * @return The list for the EmailReportHandler to parse out information
	 * @throws SQLException
	 */
	private synchronized ArrayList<SiteRecord> selectEmergencyReportLog(Date startTime, Date endTime) throws SQLException
	{
		ArrayList<SiteRecord> siteRecords = new ArrayList<>();
		Timestamp startTimestamp = new Timestamp(startTime.getTime());
		Timestamp endTimestamp = new Timestamp(endTime.getTime());
		
		Statement sqlStatement = dbConnection.createStatement();
		String sql = "SELECT * FROM " + LOG_TABLE + " WHERE (pingtime >= '" + startTimestamp + "' AND pingtime <= '" + endTimestamp + "')";
		
		ResultSet queryResult = sqlStatement.executeQuery(sql);

		while(queryResult.next())
		{
			String siteName = getSiteName(queryResult.getInt("fksiteid"));
			siteRecords.add(new SiteRecord(siteName, queryResult.getInt("fkstatusid"), queryResult.getTimestamp("pingtime"), queryResult.getFloat("latency")));
		}

		sqlStatement.close();
		
		return siteRecords;
	}
	
	/**
	 * Queries the site table by name to find its primary key.
	 * @param siteName     Name assigned to the PingSite
	 * @return The primary key of the PingSite entry in the site table
	 * @throws SQLException
	 */
	private synchronized int getSitePKey(String siteName) throws SQLException
	{
		int pKey = -1;

		Statement sqlStatement = dbConnection.createStatement();
		String sql = "SELECT pksiteid FROM " + SITE_TABLE + " WHERE name = '" + siteName + "'";
		ResultSet queryResult = sqlStatement.executeQuery(sql);

		if(queryResult.next())
		{
			pKey = queryResult.getInt("pksiteid");
		}
		
		sqlStatement.close();
		queryResult.close();
		
		return pKey;
	}
	
	/**
	 * Queries the site table by primary key to get the name associated with a PingSite.
	 * @param pKey     Primary key to query with
	 * @return The name associated with a PingSite
	 * @throws SQLException
	 */
	private synchronized String getSiteName(int pKey) throws SQLException
	{
		String siteName = "";
		Statement sqlStatement = dbConnection.createStatement();
		String sql = "SELECT * FROM " + SITE_TABLE + " WHERE pksiteid = '" + Integer.toString(pKey) + "'";
		ResultSet queryResult = sqlStatement.executeQuery(sql);

		if(queryResult.next())
		{
			siteName = queryResult.getString("name");
		}
		
		sqlStatement.close();
		queryResult.close();
		
		return siteName;
	}
}
