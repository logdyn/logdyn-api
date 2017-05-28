package com.github.logdyn.filters;

import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.github.logdyn.endpoints.LoggingEndpoint;
import com.github.logdyn.model.LogMessage;

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
