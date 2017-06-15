package com.logdyn.api.endpoints;

import com.logdyn.api.model.LogMessage;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;
import java.util.logging.Level;

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
			if (request instanceof HttpServletRequest)
			{
				LoggingEndpoint.log(new LogMessage((HttpServletRequest) request, Level.SEVERE, e.getLocalizedMessage()));
			}
			else
			{
				LoggingEndpoint.log(new LogMessage(Level.SEVERE, e.getLocalizedMessage()));
			}
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

	private static void setThreadLocals(final ServletRequest request)
	{
		if (request instanceof HttpServletRequest)
		{
			final HttpServletRequest httpRequest = (HttpServletRequest) request;
			LoggingFilter.setSessionId(httpRequest.getRequestedSessionId());
			final Principal userPrinciple = httpRequest.getUserPrincipal();
			if (null != userPrinciple)
			{
				LoggingFilter.setUsername(userPrinciple.getName());
			}
		}
	}

	static void clearThreadLocals()
	{
		LoggingFilter.username.remove();
		LoggingFilter.sessionId.remove();
	}

	public static String currentUsername()
	{
		return LoggingFilter.username.get();
	}

	public static String currentSessionId()
	{
		return LoggingFilter.sessionId.get();
	}

	static void setUsername(final String username)
	{
		LoggingFilter.username.set(username);
	}

	static void setSessionId(final String sessionId)
	{
		LoggingFilter.sessionId.set(sessionId);
	}
}
