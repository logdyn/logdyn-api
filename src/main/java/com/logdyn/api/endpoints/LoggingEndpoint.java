package com.logdyn.api.endpoints;

import com.logdyn.api.model.JsonFormatter;
import com.logdyn.api.model.LogMessage;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import java.io.Reader;
import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Endpoint Class used to log messages and send them to the client
 * 
 * @author Jake Lewis
 */
public class LoggingEndpoint extends Endpoint implements MessageHandler.Whole<Reader>
{
	private static final Formatter DEFAULT_FORMATTER = new JsonFormatter();

	private static final Map<String, LogSession> USER_SESSIONS = new ConcurrentHashMap<>();
	private static final Map<String, LogSession> NON_USER_SESSIONS = new ConcurrentHashMap<>();
	private static final LogSession ROOT_SESSION = new LogSession();
	
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
		final LogSession logSession = this.getLogSession();
		logSession.addWebsocketSession(session);
		//also add every session to ROOT so they can receive messages to all
		LoggingEndpoint.ROOT_SESSION.addWebsocketSession(session);
		logSession.sendMessages(session, LoggingEndpoint.ROOT_SESSION); //send message history
	}

	/**
	 *  {@inheritDoc}
	 */
	@Override
	public void onMessage(final Reader reader)
	{
		final JSONObject jsonObject = new JSONObject(new JSONTokener(reader));
		LogRecord logRecord;
		try
		{
			logRecord = JsonFormatter.fromJSON(jsonObject, this.username, this.httpSessionId);
			this.getLogSession().logMessage(logRecord, this.websocketSession);
		}
		catch (JSONException e)
		{
			logRecord = new LogMessage(Level.WARNING,
					"Failed to parse Log Record from client", this.username, this.httpSessionId);
			this.getLogSession().logMessage(logRecord, null);
		}
	}

	/**
	 *  {@inheritDoc}
	 */
	@Override
	public void onError(Session session, Throwable thr)
	{
		final LogRecord logRecord = new LogRecord(Level.SEVERE, thr.getLocalizedMessage());
		final LogSession logSession = this.getLogSession();
		try
		{
			logSession.logMessageToSession(session, logRecord);
		}
		catch (Exception e)
		{
			//an error happened while sending a message through the errored websocket
			//thats fine. just ignore it and send to the other websockets.
		}
		logSession.logMessage(logRecord, session);
		super.onError(session, thr);
	}

	/**
	 *  {@inheritDoc}
	 */
	@Override
	public void onClose(final Session session, final CloseReason closeReason)
	{
		final LogSession logSession = this.getLogSession();
		logSession.removeWebsocketSession(session);
		if (logSession != LoggingEndpoint.ROOT_SESSION)
		{
			LoggingEndpoint.ROOT_SESSION.removeWebsocketSession(session);
			logSession.logMessage(new LogMessage(
					Level.FINER, "Session closed due to " + closeReason.getReasonPhrase()),
					session);
		}
	}

	/**
	 * Sends a LogRecord to the relevant Websockets and stores it for later websockets to use.
	 * @param logRecord the LogRecord to log.
	 * @return true if the logRecord was successfully stored.
	 */
	public static boolean log(final LogRecord logRecord, final Formatter formatter)
	{
		String httpSessionId = null;
		String username = null;
		
		if (logRecord instanceof LogMessage)
		{
			httpSessionId = ((LogMessage) logRecord).getSessionId();
			username = ((LogMessage) logRecord).getUsername();
		}
		return LoggingEndpoint.getLogSession(username, httpSessionId).setFormatter(formatter).logMessage(logRecord);
	}

	/**
	 * Clears out the LogSession for a particular httpSession.
	 * @param httpSessionId the httpSession Id of the LogSession to clear
	 * @return true if a session was removed
	 */
	public static boolean clearSession(final String httpSessionId)
	{
		return LoggingEndpoint.NON_USER_SESSIONS.remove(httpSessionId) != null;
	}

	/**
	 * Clears out the LogSession for a particular user.
	 * @param username the username of the LogSession to clear
	 * @return true if a session was removed
	 */
	public static boolean clearUser(final String username)
	{
		return LoggingEndpoint.USER_SESSIONS.remove(username) != null;
	}

	/**
	 * Gets the most relevant LogSession for this Endpoint.
	 *
	 * first gets a session based on username, else trys to get one based on httpSessionId.
	 * if all else fails gets the Root session shared by everyone.
	 * @return the most relevant LogSession
	 */
	private LogSession getLogSession()
	{
		return LoggingEndpoint.getLogSession(this.username, this.httpSessionId);
	}

	/**
	 * Gets the most relevant LogSession.
	 *
	 * first gets a session based on username, else trys to get one based on httpSessionId.
	 * if all else fails gets the Root session shared by everyone.
	 * @param username a username to get or create
	 * @param httpSessionId a heepSessionId to get or create
	 * @return the most relevant LogSession
	 */
	private static LogSession getLogSession(final String username, final String httpSessionId)
	{
		LogSession result;
		if (null != username)
		{
			result = LoggingEndpoint.USER_SESSIONS.get(username);
			if (null == result)
			{
				result = new LogSession();
				LoggingEndpoint.USER_SESSIONS.put(username, result);
			}
		}
		else if (null != httpSessionId)
		{
			result = LoggingEndpoint.NON_USER_SESSIONS.get(httpSessionId);
			if (null == result)
			{
				result = new LogSession();
				LoggingEndpoint.NON_USER_SESSIONS.put(httpSessionId, result);
			}
		}
		else
		{
			result = LoggingEndpoint.ROOT_SESSION;
		}
		return result;
	}
}
