package com.logdyn.api.servlets;

import java.io.IOException;
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
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
	{
		final UUID websocketUUID = UUID.fromString(req.getParameter("uuid"));
		final String httpSessionId = req.getRequestedSessionId();
		
		LoggingEndpoint.registerWebsocket(websocketUUID, httpSessionId);
	}
}
