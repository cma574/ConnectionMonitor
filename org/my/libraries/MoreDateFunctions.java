package org.my.libraries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Library of functions related to calculation and formatting of dates.
 * @author Cory Ma
 */
public final class MoreDateFunctions
{
	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd", Locale.US);
    private final static SimpleDateFormat slashDateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
    private final static SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.US);
    private final static SimpleDateFormat fmTimestampFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a", Locale.US);
    private final static SimpleDateFormat fileTimestampFormat = new SimpleDateFormat("yyMMdd_kkmm", Locale.US);
	
	
    /**
     * Calculates the difference of two Dates in days.
     * @param subtrFromDate     Date to subtract from
     * @param subtrWithDate     Date to subtract with
     * @return The difference in days
     */
    public static double timeDiffInDays(Date subtrFromDate, Date subtrWithDate)
    {
        return (timeDiffInHours(subtrFromDate, subtrWithDate) / 24);
    }

    /**
     * Calculates the difference of two Dates in hours.
     * @param subtrFromDate     Date to subtract from
     * @param subtrWithDate     Date to subtract with
     * @return The difference in hours
     */
	public static double timeDiffInHours(Date subtrFromDate, Date subtrWithDate)
	{
		return (timeDiffInMinutes(subtrFromDate, subtrWithDate) / 60);
	}

	/**
     * Calculates the difference of two Dates in minutes.
     * @param subtrFromDate     Date to subtract from
     * @param subtrWithDate     Date to subtract with
     * @return The difference in minutes
     */
	public static double timeDiffInMinutes(Date subtrFromDate, Date subtrWithDate)
	{
		return (timeDiffInSeconds(subtrFromDate, subtrWithDate) / 60);
	}

	/**
     * Calculates the difference of two Dates in seconds.
     * @param subtrFromDate     Date to subtract from
     * @param subtrWithDate     Date to subtract with
     * @return The difference in seconds
     */
	public static double timeDiffInSeconds(Date subtrFromDate, Date subtrWithDate)
	{
		return ((double)(subtrFromDate.getTime() - subtrWithDate.getTime()) / 1000);
	}
	
	/**
	 * Rounds the time in a Date object down to the nearest quarter hour.
	 * @param roundDate     Date to round down
	 * @return The Date object with time rounded down
	 */
	public static Date roundDownToQuarterHour(Date roundDate)
	{
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(roundDate);
		
		int roundMinute = calendar.get(Calendar.MINUTE);

	    calendar.set(Calendar.MINUTE, roundMinute - (roundMinute % 15));
	    calendar.set(Calendar.SECOND, 0);

	    return calendar.getTime();
	}
	
	/**
	 * Rounds the time in a Date object to the nearest hour.
	 * @param roundDate     Date to round
	 * @return The Date object with time rounded
	 */
	public static Date roundToHour(Date roundDate)
	{
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(roundDate);

	    if (calendar.get(Calendar.MINUTE) >= 30)
	    	calendar.add(Calendar.HOUR, 1);

	    calendar.set(Calendar.MINUTE, 0);
	    calendar.set(Calendar.SECOND, 0);

	    return calendar.getTime();
	}
	
	/**
	 * Formats a Date as human readable YYMMDD text.
	 * @param formatDate     Date to format
	 * @return The Date as YYMMDD formatted text
	 */
	public static String formatDateAsYYMMDD(Date formatDate)
	{
		return dateFormat.format(formatDate);
	}
	
	/**
	 * Formats a Date as a human readable timestamp.
	 * @param formatDate     Date to format
	 * @return The Date as a timestamp
	 */
	public static String formatDateAsTimestamp(Date formatDate)
	{
		return timestampFormat.format(formatDate);
	}
	
	/**
	 * Formats a Date as human readable MM/DD/YY text.
	 * @param formatDate     Date to format
	 * @return The Date as MM/DD/YY formatted text
	 */
	public static String formatDateAsSlashMMDDYY(Date formatDate)
	{
		return slashDateFormat.format(formatDate);
	}
	
	/**
	 * Gets the current date as human readable YYMMDD text.
	 * @return The current date as YYMMDD formatted text
	 */
    public static String getTodayYYMMDD()
    {
        return dateFormat.format(new Date());
    }

    /**
	 * Gets the current date as human readable MM/DD/YY text.
	 * @return The current date as MM/DD/YY formatted text
	 */
    public static String getTodaySlashMMDDYY()
    {
        return slashDateFormat.format(new Date());
    }
    
    /**
     * Gets the current date and time as a human readable timestamp.
     * @return The current date and time as a timestamp
     */
    public static String getNowTimestamp()
    {
        return timestampFormat.format(new Date());
    }
    
    /**
     * Gets the current date and time as a human readable FileMaker timestamp.
     * @return The current date and time as a FileMaker timestamp
     */
    public static String getNowFMTimestamp()
    {
        return fmTimestampFormat.format(new Date());
    }
    
    /**
     * Gets the current date and time as a file name-friendly timestamp.
     * @return The current date and time as a file name timestamp
     */
    public static String getNowFileTimestamp()
    {
        return fileTimestampFormat.format(new Date());
    }

    /**
	 * Parses a Date from a YYMMDD formatted String.
	 * @param dateString     YYMMDD formatted date String
	 * @return Parsed Date object
	 */
    public static Date getDateFromYYMMDD(String dateString) throws ParseException
    {
        return dateFormat.parse(dateString);
    }
}
