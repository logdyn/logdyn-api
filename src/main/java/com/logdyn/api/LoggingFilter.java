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
			Logger.log(new LogRecord(Level.SEVERE, e.getLocalizedMessage()));
			throw e;
		}
		finally
		{
			Logger.clearThreadLocals();
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
			Logger.setCurrentSessionId(httpRequest.getRequestedSessionId());
			final Principal userPrinciple = httpRequest.getUserPrincipal();
			Logger.setCurrentUsername(null != userPrinciple ? userPrinciple.getName() : null);
		}
		else
		{
			Logger.clearThreadLocals();
		}
	}
}
