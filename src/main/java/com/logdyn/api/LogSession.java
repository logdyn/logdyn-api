package com.logdyn.api;

import com.logdyn.api.intergrations.LogFormatter;
import com.logdyn.api.intergrations.ToStringFormatter;

import javax.websocket.Session;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Future;
import java.util.logging.LogRecord;

class LogSession<R>
{
	/** the sessions that are part of this LogSession */
	private final Set<Session> sessions;
	/** The message history for this LogSession */
	private final SortedSet<R> messages;

	private final LogFormatter<R> formatter;

	public LogSession()
	{
		this(null);
	}

	public LogSession(final LogFormatter<R> formatter)
	{
		this(formatter, null);
	}

	public LogSession(final LogFormatter<R> formatter, final Comparator<R> comparator)
	{
		this.sessions = Collections.newSetFromMap(new ConcurrentHashMap<Session, Boolean>());
		this.messages = new ConcurrentSkipListSet<>(comparator);
		this.formatter = (null != formatter) ? formatter : new ToStringFormatter<R>();
	}

	/**
	 * Adds a websocket session that will recieve Records sent to this LogSession.
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
	 * @param record the {@link LogRecord} to send to this LogSessions websockets.
	 * @return true if the logRecord was stored as defined by {@link Collection#add}
	 */
	public boolean logMessage(final R record)
    {
    	return this.logMessage(record, null);
    }

	/**
	 * Stores the provided {@link LogRecord} and sends to the websocket sessions
	 * contained by this LogSession, excluding the provided session.
	 * @param record the {@link LogRecord} to send to this LogSessions websockets.
	 * @param exclude a {@link javax.websocket.Session} to exclude or {@code null}
	 * @return true if the logRecord was stored as defined by {@link Collection#add}
	 */
	public boolean logMessage(final R record, final Session exclude)
    {
    	boolean result = this.messages.add(record);

    	if (result)
    	{
    		for (final Session session : this.sessions)
			{
				if (!session.equals(exclude))
				{
					session.getAsyncRemote().sendText(this.formatter.format(record));
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
		final SortedSet<R> messages;
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
			return session.getAsyncRemote().sendText(this.formatter.format(messages));
		}
		return null;
	}

	/**
	 * Adds the messages and websocket session from another LogSession to this one.
	 * @param logSession the other LogSession to add to this one
	 * @return true if the LogSession changes as a result of the call
	 */
	public boolean addExistingSession(final LogSession logSession)
	{
		boolean result;
		result  = this.messages.addAll(logSession.messages);
		result |= this.sessions.addAll(logSession.sessions);
		return result;
	}
}
