package org.my.libraries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class MoreDateFunctions
{
	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd", Locale.US);
    private final static SimpleDateFormat slashDateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
	
    public static double timeDiffInDays(Date subtrFromDate, Date subtrWithDate)
    {
        return (timeDiffInHours(subtrFromDate, subtrWithDate) / 24);
    }

	public static double timeDiffInHours(Date subtrFromDate, Date subtrWithDate)
	{
		return (timeDiffInMinutes(subtrFromDate, subtrWithDate) / 60);
	}

	public static double timeDiffInMinutes(Date subtrFromDate, Date subtrWithDate)
	{
		return (timeDiffInSeconds(subtrFromDate, subtrWithDate) / 60);
	}

	public static double timeDiffInSeconds(Date subtrFromDate, Date subtrWithDate)
	{
		return ((double)(subtrFromDate.getTime() - subtrWithDate.getTime()) / 1000);
	}

    public static String getTodayYYMMDD()
    {
        return (dateFormat.format(new Date()));
    }

    public static String getTodaySlashMMDDYY()
    {
        return (slashDateFormat.format(new Date()));
    }

    public static Date getDateFromYYMMDD(String dateString) throws ParseException
    {
        return dateFormat.parse(dateString);
    }
}
