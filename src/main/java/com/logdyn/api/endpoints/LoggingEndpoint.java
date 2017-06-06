package com.logdyn.api.endpoints;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.logdyn.api.model.LogMessage;
import com.logdyn.api.model.LogRecordComparator;

/**
 * Endpoint Class used to log messages and send them to the client
 * 
 * @author Jake Lewis
 */
public class LoggingEndpoint extends Endpoint implements MessageHandler.Whole<Reader>
{
	private static final Map<UUID, Session> ENDPOINTS = new ConcurrentHashMap<>();
	private static final Map<String, Set<Session>> ENDPOINT_USAGE = new ConcurrentHashMap<>();
	private static final Map<String, SortedSet<LogRecord>> MESSAGES = new ConcurrentHashMap<>();
	
	private static final String TIMESTAMP_LABEL = "timestamp";
	private static final String MESSAGE_LABEL = "message";
	private static final String LEVEL_LABEL = "level";
	private static final String SESSION_ID_LABEL = "sessionId";
	
	private static final Level DEFAULT_LEVEL = Level.FINE;
	
	private Session websocketSession;
	private UUID websocketUUID;
	private String httpSessionId;

	@Override
	public void onOpen(final Session session, final EndpointConfig config)
	{
		this.websocketSession = session;
		session.addMessageHandler(Reader.class, this);
		try
		{
			session.getAsyncRemote().setBatchingAllowed(false);
		}
		catch (IOException ioe)
		{
			LoggingEndpoint.logToClient(session, LoggingEndpoint.logRecordToJSON(new LogRecord(Level.WARNING, ioe.getMessage())));
		}
		
		this.websocketUUID = UUID.randomUUID();
		LoggingEndpoint.ENDPOINTS.put(this.websocketUUID, session);
		logToClient(session, (new JSONObject().put("uuid", this.websocketUUID)).toString());
	}
	
	@Override
	public void onMessage(final Reader jsonString)
	{
		final JSONObject jsonObject = new JSONObject(new JSONTokener(jsonString));
		
		if (jsonObject.has("httpSessionId"))
		{
			this.httpSessionId = jsonObject.getString("httpSessionId");
			Set<Session> set = LoggingEndpoint.ENDPOINT_USAGE.get(this.httpSessionId);
			if (null == set)
			{
				set = new ConcurrentHashMap<Session, Void>().keySet();
				LoggingEndpoint.ENDPOINT_USAGE.put(this.httpSessionId, set);
			}
			set.add(this.websocketSession);
			
			final SortedSet<LogRecord> messageQueue = LoggingEndpoint.MESSAGES.get(this.httpSessionId);
			if (null != messageQueue && !messageQueue.isEmpty())
			{
				LoggingEndpoint.logToClient(this.websocketSession, new JSONArray(messageQueue).toString());
			}
			LoggingEndpoint.log(new LogMessage(this.httpSessionId, Level.FINER, String.format("Logging Session with id '%s' opened", this.websocketSession.getId())));
		}
		else
		{
			try
			{
				jsonObject.put("sessionId", this.httpSessionId);
				final LogMessage logMessage = new LogMessage(jsonObject.optString(LoggingEndpoint.SESSION_ID_LABEL, null),
						LoggingEndpoint.parseLevel(jsonObject),
						jsonObject.optString(LoggingEndpoint.MESSAGE_LABEL, null),
						jsonObject.optLong(LoggingEndpoint.TIMESTAMP_LABEL, System.currentTimeMillis()));
				LoggingEndpoint.queueMessage(logMessage);
				
				for (Session websocketSession : LoggingEndpoint.ENDPOINT_USAGE.get(this.httpSessionId))
				{
					if (!this.websocketSession.equals(websocketSession))
					{
						LoggingEndpoint.logToClient(websocketSession, LoggingEndpoint.logRecordToJSON(logMessage));
					}
				}
			}
			catch (JSONException ex)
			{
				LoggingEndpoint.log(new LogMessage(this.httpSessionId, Level.WARNING, ex.getMessage()));
			}
		}
		
	}

	@Override
	public void onClose(final Session session, final CloseReason closeReason)
	{
		final Set<Session> set = LoggingEndpoint.ENDPOINT_USAGE.get(this.httpSessionId);
		set.remove(session);
		final LogMessage message = new LogMessage(this.httpSessionId, Level.FINER, String.format("Logging session with id '%s' closed", session.getId()));
		LoggingEndpoint.queueMessage(message);
		if (set.isEmpty())
		{
			LoggingEndpoint.ENDPOINT_USAGE.remove(this.httpSessionId);
		}
		else
		{
			LoggingEndpoint.logToClient(set, message);
		}
	}
	
	/**
	 * Used when initially opening a websocket, adds websocket to list of endpoints
	 * @param websocketId
	 * @param username
	 * @param httpSessionId
	 */
	public static void registerWebsocket(UUID websocketId, String username, String httpSessionId)
	{
		Session websocketSession = LoggingEndpoint.ENDPOINTS.get(websocketId);
		Set<Session> set;
		
		if (null != username)
		{
			set = LoggingEndpoint.ENDPOINT_USAGE.get(username);			
			if (null == set)
			{
				set = new ConcurrentHashMap<Session, Void>().keySet();
				LoggingEndpoint.ENDPOINT_USAGE.put(username, set);
			}			
			set.add(websocketSession);
		}
		
		set = LoggingEndpoint.ENDPOINT_USAGE.get(httpSessionId);		
		if (null == set)
		{
			set = new ConcurrentHashMap<Session, Void>().keySet();
			LoggingEndpoint.ENDPOINT_USAGE.put(httpSessionId, set);
		}		
		set.add(websocketSession);
		
		LoggingEndpoint.ENDPOINTS.remove(websocketId);
	}

	/**
	 * Logs a message to the client, see {@link LoggingEndpoint#log(LogRecord, boolean)}, defaults to queueing the message
	 * @param logRecord The {@link LogRecord} to log, acts as a LogMessage with a {@code null} httpSessionId
	 */
	public static void log(final LogRecord logRecord)
	{
		LoggingEndpoint.log(logRecord, true);
	}
	
	/**
	 * Logs a message to the endpoint for that message, if no message is specified it will log to all endpoints
	 * @param logRecord The {@link LogMessage} to log
	 * @param queue boolean value, if true queue the message
	 */
	public static void log(final LogRecord logRecord, final boolean queue)
	{
		final String sessionId = LoggingEndpoint.getSessionId(logRecord);
		
		// If sessionID is not specified, notify all endpoints
		if (null == sessionId)
		{
			for (final Set<Session> websocketSessions : LoggingEndpoint.ENDPOINT_USAGE.values())
			{
				LoggingEndpoint.logToClient(websocketSessions, logRecord);
			}

			// Queue message for all sessions
			if (queue)
			{
				LoggingEndpoint.queueMessageToAll(logRecord);
			}
		}
		else
		{
			final Set<Session> websocketSessions = LoggingEndpoint.ENDPOINT_USAGE.get(sessionId);
			if (null != websocketSessions)
			{
				LoggingEndpoint.logToClient(websocketSessions, logRecord);

				if (queue)
				{
					LoggingEndpoint.queueMessage((LogMessage) logRecord);
				}
			}
			else
			{
				LoggingEndpoint.log(new LogMessage(Level.WARNING,
						String.format("No LoggingEndpoints registered for session '%s'", sessionId)));
			}
		}
		if (logRecord.getLevel().intValue() > Level.WARNING.intValue())
		{
			System.err.println(logRecord);
		}
		else
		{
			System.out.println(logRecord);
		}
	}

	private static void queueMessageToAll(final LogRecord logRecord)
	{
		for (final SortedSet<LogRecord> messageSet : LoggingEndpoint.MESSAGES.values())
		{
			messageSet.add(logRecord);
		}
	}
	

	/**
	 * Adds a message to the queue of messages for that session ID
	 * 
	 * @param logMessage The {@link LogMessage} to queue
	 */
	private static void queueMessage(final LogMessage logMessage)
	{
		SortedSet<LogRecord> messageQueue = LoggingEndpoint.MESSAGES.get(logMessage.getSessionId());

		if (null == messageQueue)
		{
			messageQueue = new ConcurrentSkipListSet<>(LogRecordComparator.COMPARATOR);
			LoggingEndpoint.MESSAGES.put(logMessage.getSessionId(), messageQueue);
		}

		messageQueue.add(logMessage);
	}

	/**
	 * Logs the JSON message to every provided endpoint
	 * 
	 * @param websocketSessions The endpoints to send the message to
	 * @param message The message
	 */
	private static void logToClient(final Collection<Session> websocketSessions, final LogRecord message)
	{
		for (final Session websocketSession : websocketSessions)
		{
			LoggingEndpoint.logToClient(websocketSession, LoggingEndpoint.logRecordToJSON(message));
		}
	}

	/**
	 * Send an array of messages to an endpoint
	 * 
	 * @param session The session to send the messages to
	 * @param message the message to send
	 */
	private static Future<Void> logToClient(final Session session, final String message)
	{
		return session.getAsyncRemote().sendText(message);
	}

	public static void clearSession(final String id)
	{
		LoggingEndpoint.MESSAGES.remove(id);
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
			return Level.parse(levelName);
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
