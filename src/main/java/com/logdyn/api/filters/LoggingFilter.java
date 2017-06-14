package com.logdyn.api.filters;

import com.logdyn.api.endpoints.LoggingEndpoint;
import com.logdyn.api.model.LogMessage;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
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
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	@Override
	public void init(final FilterConfig fConfig) throws ServletException
	{
		// NOOP
	}
}
