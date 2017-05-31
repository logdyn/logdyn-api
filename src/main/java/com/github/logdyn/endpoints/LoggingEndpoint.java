package com.github.logdyn.endpoints;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
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

import com.github.logdyn.model.LogMessage;

/**
 * Endpoint Class used to log messages and send them to the client
 * 
 * @author Jake Lewis
 */
public class LoggingEndpoint extends Endpoint implements MessageHandler.Whole<Reader>
{

	private static final Map<String, Set<Session>> ENDPOINTS = new HashMap<>();
	private static final Map<String, SortedSet<LogMessage>> MESSAGES = new HashMap<>();
	private Session session;
	private String httpSessionId;

	@Override
	public void onOpen(final Session session, final EndpointConfig config)
	{
		this.session = session;
		session.addMessageHandler(Reader.class, this);
		try
		{
			session.getAsyncRemote().setBatchingAllowed(false);
		}
		catch (IOException ioe)
		{
			LoggingEndpoint.logToClient(session, new LogMessage(Level.WARNING, ioe.getMessage()).toJSONString());
		}
	}
	
	@Override
	public void onMessage(final Reader jsonString)
	{
		final JSONObject jsonObject = new JSONObject(new JSONTokener(jsonString));
		
		if (jsonObject.has("httpSessionId"))
		{
			this.httpSessionId = jsonObject.getString("httpSessionId");
			Set<Session> set = LoggingEndpoint.ENDPOINTS.get(this.httpSessionId);
			if (null == set)
			{
				set = new HashSet<>();
				LoggingEndpoint.ENDPOINTS.put(this.httpSessionId, set);
			}
			set.add(this.session);
			
			final SortedSet<LogMessage> messageQueue = LoggingEndpoint.MESSAGES.get(this.httpSessionId);
			if (null != messageQueue && !messageQueue.isEmpty())
			{
				LoggingEndpoint.logToClient(this.session, new JSONArray(messageQueue).toString());
			}
			LoggingEndpoint.log(new LogMessage(this.httpSessionId, Level.FINER, String.format("Logging Session with id '%s' opened", this.session.getId())));
		}
		else
		{
			try
			{
				jsonObject.put("sessionId", this.httpSessionId);
				final LogMessage logMessage = new LogMessage(jsonObject);
				LoggingEndpoint.queueMessage(logMessage);
				
				for (Session websocketSession : LoggingEndpoint.ENDPOINTS.get(this.httpSessionId))
				{
					if (!this.session.equals(websocketSession))
					{
						LoggingEndpoint.logToClient(websocketSession, logMessage.toJSONString());
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
		final Set<Session> set = LoggingEndpoint.ENDPOINTS.get(this.httpSessionId);
		set.remove(session);
		final LogMessage message = new LogMessage(this.httpSessionId, Level.FINER, String.format("Logging session with id '%s' closed", session.getId()));
		LoggingEndpoint.queueMessage(message);
		if (set.isEmpty())
		{
			LoggingEndpoint.ENDPOINTS.remove(this.httpSessionId);
		}
		else
		{
			LoggingEndpoint.logToClient(set, message);
		}
	}

	/**
	 * Logs a message to the client, see {@link LoggingEndpoint#log(LogMessage, boolean)}, defaults to queueing the message
	 * @param logMessage The {@link LogMessage} to log
	 */
	public static void log(final LogMessage logMessage)
	{
		LoggingEndpoint.log(logMessage, true);
	}

	/**
	 * Logs a message to the endpoint for that message, if no message is specified it will log to all endpoints
	 * @param logMessage The {@link LogMessage} to log
	 * @param queue boolean value, if true queue the message
	 */
	public static void log(final LogMessage logMessage, final boolean queue)
	{
		final String sessionId = logMessage.getSessionId();
		
		// If sessionID is not specified, notify all endpoints
		if (null == sessionId)
		{
			for (final Set<Session> websocketSessions : LoggingEndpoint.ENDPOINTS.values())
			{
				LoggingEndpoint.logToClient(websocketSessions, logMessage);
			}

			// Queue message for all sessions
			if (queue)
			{
				LoggingEndpoint.queueMessageToAll(logMessage);
			}
		}
		else
		{
			final Set<Session> websocketSessions = LoggingEndpoint.ENDPOINTS.get(sessionId);
			if (null != websocketSessions)
			{
				LoggingEndpoint.logToClient(websocketSessions, logMessage);

				if (queue)
				{
					LoggingEndpoint.queueMessage(logMessage);
				}
			}
			else
			{
				LoggingEndpoint.log(new LogMessage(Level.WARNING,
						String.format("No LoggingEndpoints registered for session '%s'", sessionId)));
			}
		}
		if (logMessage.getLevel().intValue() > Level.WARNING.intValue())
		{
			System.err.println(logMessage);
		}
		else
		{
			System.out.println(logMessage);
		}
	}
	
	/**
	 * Logs a message to the client, see {@link LoggingEndpoint#log(LogRecord, boolean)}, defaults to queueing the message
	 * @param logRecord The {@link LogRecord} to log, acts as a LogMessage with a {@code null} httpSessionId
	 */
	public void log(LogRecord logRecord)
	{
		LoggingEndpoint.log(new LogMessage(null, logRecord.getLevel(), logRecord.getMessage(), logRecord.getMillis()));
	}
	
	/**
	 * Logs a message to the client, see {@link LoggingEndpoint#log(LogRecord, boolean)}
	 * @param logRecord The {@link LogRecord} to log, acts as a LogMessage with a {@code null} httpSessionId
	 * @param queue If false does not queue the message for storage, if always true log(LogRecord) can be used
	 */
	public void log(final LogRecord logRecord, final boolean queue)
	{
		LoggingEndpoint.log(new LogMessage(null, logRecord.getLevel(), logRecord.getMessage(), logRecord.getMillis()), queue);
	}

	private static void queueMessageToAll(final LogMessage logMessage)
	{
		for (final SortedSet<LogMessage> messageSet : LoggingEndpoint.MESSAGES.values())
		{
			messageSet.add(logMessage);
		}
	}
	

	/**
	 * Adds a message to the queue of messages for that session ID
	 * 
	 * @param httpSessionId The HTTP session ID
	 * @param logMessage The {@link LogMessage} to queue
	 */
	private static void queueMessage(final LogMessage logMessage)
	{
		SortedSet<LogMessage> messageQueue = LoggingEndpoint.MESSAGES.get(logMessage.getSessionId());

		if (null == messageQueue)
		{
			messageQueue = new TreeSet<>();
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
	private static void logToClient(final Collection<Session> websocketSessions, final LogMessage message)
	{
		for (final Session websocketSession : websocketSessions)
		{
			LoggingEndpoint.logToClient(websocketSession, message.toJSONString());
		}
	}

	/**
	 * Send an array of messages to an endpoint
	 * 
	 * @param endpoint
	 *            The endpoint to send the messages to
	 * @param jsonArray
	 *            A JSON array of log messages
	 */
	private static Future<Void> logToClient(final Session session, final String message)
	{
		return session.getAsyncRemote().sendText(message);
	}

	public static void clearSession(final String id)
	{
		LoggingEndpoint.MESSAGES.remove(id);
	}
}