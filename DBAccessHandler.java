import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/**
 * An object that handles access to DataBase and performs necessary queries and input to it. Since multiple
 * threads require access to the database concurrently, one object should be shared and functions
 * need to be threadsafe. 
 * @author Cory Ma
 */
public class DBAccessHandler
{
	//JDBC Driver Name and Database URL
	private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	private final String DB_URL = "jdbc:mysql://localhost/cma574_db";
	
	//Database Credentials
	private final String USER = "cma574";
	private final String PASSWORD = "heGep54uZ";
	
	//Table Names
	private final String LOG_TABLE = "log";
	private final String SITE_TABLE = "site";
	
	private Connection dbConnection;
	
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
			ipInsertString = ipAddress;
		if(latency != -1)
			latencyInsertString = Double.toString(latency);
		
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
	 * Performs a query on the log table to get information required for a regular report email.
	 * @param siteName      Name assigned to the PingSite
	 * @param startTime     Time to start the query's WHERE clause from
	 * @param endTime       Time to end the query's WHERE clause at
	 * @return The list for the EmailReportHandler to parse out information
	 * @throws SQLException
	 */
	public synchronized ArrayList<SiteRecord> selectReportLog(String siteName, Date startTime, Date endTime) throws SQLException
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
	public synchronized ArrayList<SiteRecord> selectEmergencyReportLog(Date startTime, Date endTime) throws SQLException
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
	public synchronized int getSitePKey(String siteName) throws SQLException
	{
		int pKey = -1;

		Statement sqlStatement = dbConnection.createStatement();
		String sql = "SELECT pksiteid FROM " + SITE_TABLE + " WHERE name = '" + siteName + "'";
		ResultSet queryResult = sqlStatement.executeQuery(sql);

		if(queryResult.next())
			pKey = queryResult.getInt("pksiteid");
		
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
	public synchronized String getSiteName(int pKey) throws SQLException
	{
		String siteName = "";
		Statement sqlStatement = dbConnection.createStatement();
		String sql = "SELECT * FROM " + SITE_TABLE + " WHERE pksiteid = '" + Integer.toString(pKey) + "'";
		ResultSet queryResult = sqlStatement.executeQuery(sql);

		if(queryResult.next())
			siteName = queryResult.getString("name");
		
		sqlStatement.close();
		queryResult.close();
		
		return siteName;
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
}
