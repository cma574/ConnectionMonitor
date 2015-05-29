package org.my.libraries;

import java.util.ArrayList;

/**
 * Library of functions related to mathematical operations.
 * @author Cory Ma
 */
public final class MoreMath
{
	/**
	 * Calculates the standard deviation of a list of values.
	 * @param values     List of values
	 * @return The standard deviation of the values
	 */
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
	
	/**
	 * Calculates the mean of a list of values.
	 * @param values     List of values
	 * @return The mean of the values
	 */
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
	
	/**
	 * Calculates the sum of a list of values.
	 * @param values     List of values
	 * @return The sum of the values
	 */
	public static double sum(ArrayList<Double> values)
	{
		double sum = 0;
		int numVals;
		
		numVals = values.size();
		for(int count = 0; count < numVals; count++)
			sum += values.get(count);
		
		return sum;
	}
	
	/**
	 * Calculates a positive modulo of two values.
	 * @param dividend     Dividend of the calculation
	 * @param divisor      Divisor of the calculation
	 * @return The positive modulo of the values
	 */
	public static int modulo(int dividend, int divisor)
	{
		return (((dividend % divisor) + divisor) % divisor);
	}
}
