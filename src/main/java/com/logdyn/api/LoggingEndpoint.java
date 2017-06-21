package com.logdyn.api;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import java.io.Reader;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Endpoint Class used to log messages and send them to the client
 * 
 * @author Jake Lewis
 */
public class LoggingEndpoint extends Endpoint implements MessageHandler.Whole<Reader>
{
	private String httpSessionId = null;
	private String username;
	private Session websocketSession;

	/**
	 *  {@inheritDoc}
	 */
	@Override
	public void onOpen(final Session session, final EndpointConfig config)
	{
		//add its self as a message handler. this allows access to username & httpSessionId fields
		session.addMessageHandler(Reader.class, this);
		this.websocketSession = session;

		final Principal userPrinciple = session.getUserPrincipal();
		this.username = (null != userPrinciple) ? userPrinciple.getName() : null;
		//get httpSession from config. see LoggingEndpointConfigurator class.
		final Object httpSession = config.getUserProperties().get(HttpSession.class.getName());
		if (httpSession instanceof HttpSession)
		{
			this.httpSessionId = ((HttpSession) httpSession).getId();
		}
		// add websocket session to relevant logSession
		Logger.addSession(session, this.username, this.httpSessionId);
	}

	/**
	 *  {@inheritDoc}
	 */
	@Override
	public void onMessage(final Reader reader)
	{
		this.setThreadLocals();

		LogRecord logRecord;
		try
		{
			final JSONObject jsonObject = new JSONObject(new JSONTokener(reader));
			logRecord = LogRecordUtils.fromJSON(jsonObject, this.username, this.httpSessionId);
			Logger.log(logRecord, this.websocketSession);
		}
		catch (JSONException e)
		{
			logRecord = new LogRecord(Level.WARNING,"Failed to parse Log Record from client");
			Logger.log(logRecord, null);
		}
		finally
		{
			Logger.clearThreadLocals();
		}
	}

	/**
	 *  {@inheritDoc}
	 */
	@Override
	public void onError(final Session session, final Throwable thr)
	{
		this.setThreadLocals();
		final LogRecord logRecord = new LogRecord(Level.SEVERE, thr.getLocalizedMessage());
		try
		{
			session.getAsyncRemote().sendText(LogRecordUtils.toJSON(logRecord));
		}
		catch (Exception e)
		{
			//an error happened while sending a message through the errored websocket
			//thats fine. just ignore it and send to the other websockets.
		}
		Logger.log(logRecord, session);
		super.onError(session, thr);
		Logger.clearThreadLocals();
	}

	/**
	 *  {@inheritDoc}
	 */
	@Override
	public void onClose(final Session session, final CloseReason closeReason)
	{
		Logger.removeSession(session, this.username, this.httpSessionId);
	}

	private void setThreadLocals()
	{
		Logger.setCurrentUsername(this.username);
		Logger.setCurrentSessionId(this.httpSessionId);
	}
}
