package org.my.libraries;

import java.util.ArrayList;

public final class MoreMath
{
	public static double stdDev(ArrayList<Double> values)
	{
		double stdDev = 0;
		double average;
		double summation = 0;
		
		int numVals;
		
		numVals = values.size();
		if(numVals > 0)
		{
			average = mean(values);
			for(int count = 0; count < numVals; count++)
				summation += Math.pow((average - values.get(count)), 2);
			stdDev = Math.sqrt(summation/numVals);
		}
		
		return stdDev;
	}
	
	public static double mean(ArrayList<Double> values)
	{
		double mean = 0;
		double total;
		int numVals;
		
		numVals = values.size();
		if(numVals > 0)
		{
			total = sum(values);
			mean = total/numVals;
		}
		
		return mean;
	}
	
	public static double sum(ArrayList<Double> values)
	{
		double sum = 0;
		int numVals;
		
		numVals = values.size();
		for(int count = 0; count < numVals; count++)
			sum += values.get(count);
		
		return sum;
	}
	
	//Performs modulo with a guaranteed positive result
	public static int modulo(int dividend, int divisor)
	{
		return (((dividend % divisor) + divisor) % divisor);
	}
}
