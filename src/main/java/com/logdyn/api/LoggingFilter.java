package com.logdyn.api;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Servlet Filter implementation class LoggingFilter
 */
public class LoggingFilter implements Filter
{
	private static final ThreadLocal<String> username = new ThreadLocal<>();

	private static final ThreadLocal<String> sessionId = new ThreadLocal<>();
	/**
	 * @see Filter#destroy()
	 */
	@Override
	public void destroy()
	{
		// NOOP
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException
	{
		try
		{
			LoggingFilter.setThreadLocals(request);
			chain.doFilter(request, response);
		}
		catch (final Throwable e)
		{
			LogRecord record;
			if (request instanceof HttpServletRequest)
			{
				record = new LogMessage(Level.SEVERE, e.getLocalizedMessage(), (HttpServletRequest) request);
			}
			else
			{
				record = new LogRecord(Level.SEVERE, e.getLocalizedMessage());
				record.setMillis(System.currentTimeMillis());
			}
			LoggingEndpoint.log(record);
			throw e;
		}
		finally
		{
			LoggingFilter.clearThreadLocals();
		}
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	@Override
	public void init(final FilterConfig fConfig) throws ServletException
	{
		// NOOP
	}

	/**
	 * Sets the current {@link ThreadLocal}s based on a request being processed.
	 * @param request the request to get values from
	 */
	private static void setThreadLocals(final ServletRequest request)
	{
		if (request instanceof HttpServletRequest)
		{
			final HttpServletRequest httpRequest = (HttpServletRequest) request;
			LoggingFilter.setSessionId(httpRequest.getRequestedSessionId());
			final Principal userPrinciple = httpRequest.getUserPrincipal();
			LoggingFilter.setUsername(null != userPrinciple ? userPrinciple.getName() : null);
		}
		else
		{
			LoggingFilter.clearThreadLocals();
		}
	}

	/**
	 * removes current values for thread local variables.
	 */
	static void clearThreadLocals()
	{
		LoggingFilter.username.remove();
		LoggingFilter.sessionId.remove();
	}

	/**
	 * gets the username of the request currently being processed.
	 * @return the username
	 */
	public static String currentUsername()
	{
		return LoggingFilter.username.get();
	}

	/**
	 * gets the httpSessionId of the request currently being processed.
	 * @return the session Id
	 */
	public static String currentSessionId()
	{
		return LoggingFilter.sessionId.get();
	}

	/**
	 * sets the username of the request that is currently being processed.
	 * @param username the username to set
	 */
	static void setUsername(final String username)
	{
		LoggingFilter.username.set(username);
	}

	/**
	 * sets the httpSessionId of the request that is currently being processed.
	 * @param sessionId the session ID to set
	 */
	static void setSessionId(final String sessionId)
	{
		LoggingFilter.sessionId.set(sessionId);
	}
}
