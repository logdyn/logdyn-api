package com.logdyn.api.endpoints;

import com.logdyn.api.model.JsLevel;
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
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Endpoint Class used to log messages and send them to the client
 * 
 * @author Jake Lewis
 */
public class LoggingEndpoint extends Endpoint implements MessageHandler.Whole<Reader>
{
	private static final String TIMESTAMP_LABEL = "timestamp";
	private static final String MESSAGE_LABEL = "message";
	private static final String LEVEL_LABEL = "level";
	
	private static final Map<String, LogSession> USER_SESSIONS = new ConcurrentHashMap<>();
	private static final Map<String, LogSession> NON_USER_SESSIONS = new ConcurrentHashMap<>();
	private static final LogSession ROOT_SESSION = new LogSession();
	
	private static final Level DEFAULT_LEVEL = Level.FINE;
	
	private String httpSessionId = null;
	private String username;
	private Session websocketSession;

	@Override
	public void onOpen(final Session session, final EndpointConfig config)
	{
		session.addMessageHandler(Reader.class, this);
		this.websocketSession = session;

		final Principal userPrinciple = session.getUserPrincipal();
		this.username = (null != userPrinciple) ? userPrinciple.getName() : null;
		final Object httpSession = config.getUserProperties().get(HttpSession.class.getName());
		if (httpSession instanceof HttpSession)
		{
			this.httpSessionId = ((HttpSession) httpSession).getId();
		}

		final LogSession logSession = this.getLogSession();
		logSession.addWebsocketSession(session);
		logSession.sendMessages(session);
	}

	private LogSession getLogSession()
	{
		return LoggingEndpoint.getLogSession(this.username, this.httpSessionId);
	}
	
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

	@Override
	public void onMessage(final Reader reader)
	{
		final JSONObject jsonObject = new JSONObject(new JSONTokener(reader));
		LogRecord logRecord;
		try
		{
			logRecord = new LogMessage(
					LoggingEndpoint.parseLevel(jsonObject),
					jsonObject.getString(LoggingEndpoint.MESSAGE_LABEL),
					this.username, this.httpSessionId,
					jsonObject.optLong(LoggingEndpoint.TIMESTAMP_LABEL, System.currentTimeMillis()));
			this.getLogSession().logMessage(logRecord, this.websocketSession);
		}
		catch (JSONException e)
		{
			logRecord = new LogMessage(Level.WARNING,
					"Failed to parse Log Record from client", this.username, this.httpSessionId);
			this.getLogSession().logMessage(logRecord, null);
		}
	}

	@Override
	public void onClose(final Session session, final CloseReason closeReason)
	{
		this.getLogSession().removeWebsocketSession(session);
		LoggingEndpoint.ROOT_SESSION.removeWebsocketSession(session);
	}
	
	public static void log(final LogRecord logRecord)
	{
		String httpSessionId = null;
		String username = null;
		
		if (logRecord instanceof LogMessage)
		{
			httpSessionId = ((LogMessage) logRecord).getSessionId();
			username = ((LogMessage) logRecord).getUsername();
		}
		
		LoggingEndpoint.getLogSession(username, httpSessionId).logMessage(logRecord);
	}

	public static void clearSession(final String httpSessionId)
	{
		LoggingEndpoint.NON_USER_SESSIONS.remove(httpSessionId);
	}
	
	private static Level parseLevel(final JSONObject jsonObject)
	{
		final String levelName = jsonObject.optString(LoggingEndpoint.LEVEL_LABEL, null);
		if(null == levelName)
		{
			return LoggingEndpoint.DEFAULT_LEVEL;
		}
		else
		{
			return JsLevel.parse(levelName);
		}
	}
}
