package com.logdyn.api.endpoints;

import com.logdyn.api.model.JsLevel;
import com.logdyn.api.model.LogMessage;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import java.io.IOException;
import java.io.Reader;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
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
	private static final Map<String, Map<String, Set<Session>>> ENDPOINTS = new ConcurrentHashMap<>();
	private static final Map<String, SortedSet<LogRecord>> MESSAGES = new ConcurrentHashMap<>();
	
	private static final String TIMESTAMP_LABEL = "timestamp";
	private static final String MESSAGE_LABEL = "message";
	private static final String LEVEL_LABEL = "level";
	private static final String SESSION_ID_LABEL = "sessionId";
	
	private static final Level DEFAULT_LEVEL = Level.FINE;
	
	private Session websocketSession = null;
	private String httpSessionId = null;
	private String username = null;

	@Override
	public void onOpen(final Session session, final EndpointConfig config)
	{
		final Object httpSession = config.getUserProperties().get(HttpSession.class.getName());
		if (httpSession instanceof HttpSession)
		{
			this.httpSessionId = ((HttpSession) httpSession).getId();
		}
		final Principal userPrinciple = session.getUserPrincipal();
		this.username = (null != userPrinciple) ? userPrinciple.getName() : String.valueOf(null);

		Map<String, Set<Session>> httpSessionMap = ENDPOINTS.get(this.username);
		if (null == httpSessionMap)
		{
			httpSessionMap = new ConcurrentHashMap<>();
			ENDPOINTS.put(this.username, httpSessionMap);
		}
		Set<Session> sessionSet = httpSessionMap.get(this.httpSessionId);
		if (null == sessionSet)
		{
			sessionSet = Collections.newSetFromMap(new ConcurrentHashMap<Session, Boolean>());
			httpSessionMap.put(this.httpSessionId, sessionSet);
		}
		sessionSet.add(session);
		try
		{
			session.getAsyncRemote().setBatchingAllowed(true);
		}
		catch (final IOException ioe)
		{
			LoggingEndpoint.logToClient(session, LoggingEndpoint.logRecordToJSON(new LogRecord(Level.WARNING, ioe.getMessage())));
		}

	}

	@Override
	public void onMessage(Reader reader)
	{
		final JSONObject jsonObject = new JSONObject(new JSONTokener(reader));
	}

	@Override
	public void onClose(final Session session, final CloseReason closeReason)
	{

	}

	public static void clearSession(final String id)
	{

	}
	
	private static String getSessionId(final LogRecord logRecord)
	{
		String sessionId = null;
		
		if (logRecord instanceof LogMessage)
		{
			sessionId = ((LogMessage) logRecord).getSessionId();
		}
		
		return sessionId;
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
	
	private static String logRecordToJSON(final LogRecord logRecord)
	{
		return new JSONObject()
				.put(LoggingEndpoint.SESSION_ID_LABEL, LoggingEndpoint.getSessionId(logRecord))
				.put(LoggingEndpoint.LEVEL_LABEL, logRecord.getLevel().getName())
				.put(LoggingEndpoint.MESSAGE_LABEL, logRecord.getMessage())
				.put(LoggingEndpoint.TIMESTAMP_LABEL, Long.valueOf(logRecord.getMillis()))
				.toString();
	}

}
