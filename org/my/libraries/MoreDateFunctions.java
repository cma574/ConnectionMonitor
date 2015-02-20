package org.my.libraries;

import java.util.Date;

public final class MoreDateFunctions
{
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
}
