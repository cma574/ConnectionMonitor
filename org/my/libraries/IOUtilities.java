package org.my.libraries;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Library of commonly used IO related functions.
 * @author Cory Ma
 */
public class IOUtilities
{
	/**
	 * Reads a line of text from the console input.
	 * @return The input received from the console
	 * @throws IOException
	 */
	public static String readConsoleLine() throws IOException
	{
		String input = "";
		Console console = System.console();
	    if(console == null)
	    {
	    	input = new BufferedReader(new InputStreamReader(System.in)).readLine();
	    }
	    else
	    {
	    	input = console.readLine();
	    }
	    
		return input;
	}
	
	/**
	 * Reads a line of text from the console input with echoing disabled unless run from an IDE.
	 * @return The input received from the console
	 * @throws IOException
	 */
	public static String readPassword() throws IOException
	{
		String input = "";
		Console console = System.console();
	    if(console == null)
	    {
	    	input = new BufferedReader(new InputStreamReader(System.in)).readLine();
	    }
	    else
	    {
	    	input = new String(console.readPassword());
	    }
	    
		return input;
	}
	
	/**
	 * Calls close() on a Closeable object and ignores any IOExceptions thrown.
	 * @param closeable     Object to close
	 */
	public static void closeCloseable(Closeable closeable)
	{
		try
		{
			closeable.close();
		}
		catch(IOException e)
		{}
	}
	
	/**
	 * Determines whether an input's response was Yes, where it is the default for an empty string.
	 * @param input     String to parse
	 * @return true if the response was Yes, Y, or empty, otherwise false
	 */
	public static boolean parseDefaultYes(String input)
	{
		input = input.toUpperCase();
		boolean isYes = (input.length() == 0 || input.equals("Y") || input.equals("YES"));
		
		return isYes;
	}
}
