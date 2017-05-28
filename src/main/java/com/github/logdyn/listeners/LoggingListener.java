package com.github.logdyn.listeners;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.github.logdyn.endpoints.LoggingEndpoint;

/**
 * Application Lifecycle Listener implementation class HttpListener
 *
 */
public class LoggingListener implements HttpSessionListener 
{
	/**
     * @see HttpSessionListener#sessionCreated(HttpSessionEvent)
     */
    @Override
	public void sessionCreated(final HttpSessionEvent se)  { 
         //NOOP
    }

	/**
     * @see HttpSessionListener#sessionDestroyed(HttpSessionEvent)
     */
    @Override
	public void sessionDestroyed(final HttpSessionEvent se)  { 
         LoggingEndpoint.clearSession(se.getSession().getId());
    }
}
