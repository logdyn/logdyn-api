package com.logdyn.api.servlets;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.logdyn.api.endpoints.LoggingEndpoint;

public class WebsocketInitServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		final String websocketUUID = req.getParameter("uuid");
		final String httpSessionId = req.getRequestedSessionId();
		final Principal userPrincipal = req.getUserPrincipal();
		String key;
		
		if (null != userPrincipal)
		{
			key = userPrincipal.getName();
		}
		else
		{
			key = httpSessionId;
		}
		
		LoggingEndpoint.registerEndpoint(websocketUUID, key);
	}
}
