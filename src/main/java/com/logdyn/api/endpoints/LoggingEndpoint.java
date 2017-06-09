package com.logdyn.api.endpoints;

import java.io.Reader;
import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.logdyn.api.model.JsLevel;
import com.logdyn.api.model.LogMessage;

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
	private static final String SESSION_ID_LABEL = "sessionId";
	
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
		final Principal userPrinciple = session.getUserPrincipal();
		this.username = (null != userPrinciple) ? userPrinciple.getName() : null;
		this.websocketSession = session;
		this.websocketSession.addMessageHandler(Reader.class, this);
		final Object httpSession = config.getUserProperties().get(HttpSession.class.getName());
		if (httpSession instanceof HttpSession)
		{
			this.httpSessionId = ((HttpSession) httpSession).getId();
		}		

		getLogSession().addWebsocketSession(session);
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
		
		final LogRecord logRecord = new LogMessage(jsonObject.optString(LoggingEndpoint.SESSION_ID_LABEL, null),
				LoggingEndpoint.parseLevel(jsonObject),
				jsonObject.optString(LoggingEndpoint.MESSAGE_LABEL, null),
				jsonObject.optLong(LoggingEndpoint.TIMESTAMP_LABEL, System.currentTimeMillis()));
		
		getLogSession().logMessage(logRecord, this.websocketSession);
	}

	@Override
	public void onClose(final Session session, final CloseReason closeReason)
	{
		final LogSession logSession = getLogSession();
		logSession.removeWebsocketSession(session);
		
		if (logSession.isSessionsEmpty())
		{
			if (null != this.username)
			{
				LoggingEndpoint.USER_SESSIONS.remove(this.username);		
			}
			else if (null != httpSessionId)
			{
				LoggingEndpoint.NON_USER_SESSIONS.remove(this.httpSessionId);
			}
		}
		
		LoggingEndpoint.ROOT_SESSION.removeWebsocketSession(session);
	}
	
	public static void log(final LogRecord logRecord)
	{
		String httpSessionId = null;
		final String username = null;
		
		if (logRecord instanceof LogMessage)
		{
			httpSessionId = ((LogMessage) logRecord).getSessionId();
		}
		
		getLogSession(username, httpSessionId).logMessage(logRecord);
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
