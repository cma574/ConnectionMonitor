import java.sql.SQLException;
import java.util.Vector;

/**
 * Thread for checking to see if a regular email report needs to be sent
 * @author Cory Ma
 */
public class RegularReportThread extends ShutDownableThread
{
	private Vector<String> activePingSiteNames;
	private final int MS_PER_CHECK = 5000;
	
	private EmailReportHandler emailReportHandler;
	private DBAccessHandler dbAccessHandler;
	
	/**
	 * Constructor.
	 * @param activeSiteNames     List of active ping site names
	 * @param emailHandler        EmailReportHandler for email report synchronization
	 * @param dbHandler           DBAccessHandler to handle read and write to database
	 */
	public RegularReportThread(Vector<String> activeSiteNames, EmailReportHandler emailHandler, DBAccessHandler dbHandler)
	{
		super("RegularReportThread");
		activePingSiteNames = activeSiteNames;
		emailReportHandler = emailHandler;
		dbAccessHandler = dbHandler;
	}
	
	/**
	 * Overridden Thread run() method. This will loop until the isShutDown flag is set, checking to see
	 * if a regular email report is necessary.
	 */
	@Override
	public void run()
	{
		while(!isShutDown)
		{
			try
			{
				sleep(MS_PER_CHECK);				
				CheckNeedEmailResponse needEmail = emailReportHandler.checkReportSend();
				if(needEmail.getShouldSend())
				{
					String message = dbAccessHandler.buildRegularReportMessage(activePingSiteNames, needEmail.getStartDateQuery(), needEmail.getEndDateQuery());
					emailReportHandler.sendReport(false, message);
				}
			}
			catch(InterruptedException | SQLException e)
			{
				e.printStackTrace();
			}
		}
	}
}
