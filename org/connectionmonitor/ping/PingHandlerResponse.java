package org.connectionmonitor.ping;
/**
 * Enumeration of possible responses from the PingHandler
 * @author Cory Ma
 */
public enum PingHandlerResponse
{
	NO_PROBLEMS,
	SITE_UNREACHABLE,
	SITE_REACHABLE_AGAIN,
	SITE_LATENCY_SLOW
}
