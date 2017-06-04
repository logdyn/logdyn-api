package com.logdyn.api.servlets;

import java.io.IOException;
import java.security.Principal;
import java.util.UUID;

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
		final UUID websocketUUID = UUID.fromString(req.getParameter("uuid"));
		final String httpSessionId = req.getRequestedSessionId();
		final Principal userPrincipal = req.getUserPrincipal();
		String username = null;
		
		if (null != userPrincipal)
		{
			username = userPrincipal.getName();
		}
		
		LoggingEndpoint.registerWebsocket(websocketUUID, username, httpSessionId);
	}
}
