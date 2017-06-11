package com.logdyn.api.endpoints;

import com.logdyn.api.model.LogRecordComparator;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.websocket.Session;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Future;
import java.util.logging.LogRecord;

class LogSession
{
	/** The level label used for JSON Objects */
	private static final String LEVEL_LABEL = "level";
	/** The message label used for JSON Objects */
	private static final String MESSAGE_LABEL = "message";
	/** The timestamp label used for JSON Objects */
	private static final String TIMESTAMP_LABEL = "timestamp";

	/** the sessions that are part of this LogSession */
	private final Set<Session> sessions = Collections.newSetFromMap(new ConcurrentHashMap<Session, Boolean>());
	/** The message history for this LogSession */
	private final SortedSet<LogRecord> messages = new ConcurrentSkipListSet<>(LogRecordComparator.COMPARATOR);

	/**
	 * Adds a websocket session that will recieve LogRecords sent to this LogSession.
	 * @param session the session to add
	 * @return true if the session was added as defined by {@link Collection#add}
	 * @see Collection#add
	 */
	public boolean addWebsocketSession(final Session session)
    {
    	return this.sessions.add(session);
    }

	/**
	 * Removes a websocket session from this LogSession.
	 * @param session the session to remove
	 * @return true if the session was removed as defined by {@link Collection#remove}
	 * @see Collection#remove
	 */
	public boolean removeWebsocketSession(final Session session)
    {
    	return this.sessions.remove(session);
    }

	/**
	 * Returns true if this LogSession contains no websocket sessions
	 * @return true if this LogSession contains no websocket sessions as defined by {@link Collection#isEmpty()}
	 * @see Collection#isEmpty()
	 */
	public boolean isSessionsEmpty()
    {
    	return this.sessions.isEmpty();
    }

	/**
	 * Stores the provided {@link LogRecord} and sends to the websocket sessions
	 * contained by this LogSession.
	 * @param logRecord the {@link LogRecord} to send to this LogSessions websockets.
	 * @return true if the logRecord was stored as defined by {@link Collection#add}
	 */
	public boolean logMessage(final LogRecord logRecord)
    {
    	return this.logMessage(logRecord, null);
    }

	/**
	 * Stores the provided {@link LogRecord} and sends to the websocket sessions
	 * contained by this LogSession, excluding the provided session.
	 * @param logRecord the {@link LogRecord} to send to this LogSessions websockets.
	 * @param exclude a {@link javax.websocket.Session} to exclude or {@code null}
	 * @return true if the logRecord was stored as defined by {@link Collection#add}
	 */
	public boolean logMessage(final LogRecord logRecord, final Session exclude)
    {
    	boolean result = this.messages.add(logRecord);

    	if (result)
    	{
    		for (final Session session : this.sessions)
			{
				if (!session.equals(exclude))
				{
					session.getAsyncRemote().sendText(LogSession.logRecordToJSON(logRecord).toString());
				}
			}
		}
    	return result;
    }

	/**
	 * sends this LogSessions message history to the specified websocket session
	 * @param session the session to send this LogSessions messages to
	 * @param otherSession another Session to get messages to also send.
	 * @return the {@link Future} object representing the send operation.
	 */
	public Future<Void> sendMessages(final Session session, final LogSession otherSession)
	{
		final SortedSet<LogRecord> messages;
		if (null == otherSession || this == otherSession)
		{
			messages = this.messages;
		}
		else
		{
			messages = new TreeSet<>(this.messages);
			messages.addAll(otherSession.messages);
		}
		if (!messages.isEmpty())
		{
			final JSONArray jsonArray = new JSONArray();
			for (final LogRecord logRecord : messages)
			{
				jsonArray.put(logRecordToJSON(logRecord));
			}
			return session.getAsyncRemote().sendText(jsonArray.toString());
		}
		return null;
	}

	public void addExistingSession(final LogSession logSession)
	{
		this.messages.addAll(logSession.messages);
		this.sessions.addAll(logSession.sessions);
	}

	/**
	 * creates a {@link JSONObject} representing a provided {@link LogRecord}.
	 * @param logRecord the {@link LogRecord} to base the JSONObject on.
	 * @return the JSONObject with the values from the LogRecord.
	 */
    private static JSONObject logRecordToJSON(final LogRecord logRecord)
	{
		return new JSONObject()
				.put(LogSession.LEVEL_LABEL, logRecord.getLevel().getName())
				.put(LogSession.MESSAGE_LABEL, logRecord.getMessage())
				.put(LogSession.TIMESTAMP_LABEL, Long.valueOf(logRecord.getMillis()));
	}
}
