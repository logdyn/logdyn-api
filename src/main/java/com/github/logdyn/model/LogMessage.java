package com.github.logdyn.model;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.servlet.http.HttpServletRequest;

/**
 * Java object containing message details: sessionId, level, message, timestamp
 * @author jsjlewis96
 */
public class LogMessage extends LogRecord implements Comparable<LogMessage>
{

	/** Generated serialID **/
	private static final long serialVersionUID = -9093297911420796177L;
	
	private static final Level DEFAULT_LEVEL = Level.FINE;
	
	private final String sessionId;
	private int hashCode = -1;
	
	/**
	 * Constructor
	 * Defaults level to FINE ({@link java.util.logging.Level#FINE})
	 * @param message The message to log
	 */
	public LogMessage(final String message)
	{
		this(LogMessage.DEFAULT_LEVEL, message);
	}
	
	/**
	 * Constructor
	 * No session ID is specified, takes a level and message
	 * @param level The log level, see {@link java.util.logging.Level}
	 * @param message The message to be displayed
	 */
	public LogMessage(final Level level, final String message)
	{
		this((String) null, level, message);
	}
	
	/**
	 * Constructor
	 * Uses the {@link javax.servlet.http.HttpServletRequest#getRequestedSessionId()} to get the session ID of the request
	 * @param request The HttpServletRequest
	 * @param level The log level, see {@link java.util.logging.Level}
	 * @param message The message to be displayed
	 */
	public LogMessage(final HttpServletRequest request, final Level level, final String message)
	{
		this(request.getRequestedSessionId(), level, message);
	}
	
	/**
	 * Constructor
	 * Populates message with current system time in milliseconds
	 * @param sessionId The HttpSessionId
	 * @param level The log level, see {@link java.util.logging.Level}
	 * @param message The message to be displayed
	 */
	public LogMessage(final String sessionId, final Level level, final String message)
	{
		this(sessionId, level, message, System.currentTimeMillis());
	}

	/**
	 * Constructor
	 * Ideally other constructors should be used
	 * @param sessionId The HttpSessionId
	 * @param level The log level, see {@link java.util.logging.Level}
	 * @param message The message to be displayed
	 * @param timestamp The timestamp the message is logged at (in milliseconds)
	 */
	public LogMessage(final String sessionId, final Level level, final String message, final long timestamp)
	{
		super(level, message);
		this.sessionId = sessionId;
		this.setMillis(timestamp);
	}
	
	/**
	 * @return the sessionId
	 */
	public String getSessionId()
	{
		return this.sessionId;
	}

	@Override
	public int compareTo(final LogMessage other)
	{
		int result;
		if (other != null)
		{
			result = Long.compare(this.getMillis(), other.getMillis());
			if (result == 0)
			{
				result = Integer.compare(this.getLevel().intValue(), other.getLevel().intValue());
				if (result == 0)
				{
					//noinspection StringEquality
					result = (this.sessionId == other.sessionId) ? 0 : (this.sessionId == null ? -1 : this.sessionId.compareTo(other.sessionId));
					if (result == 0)
					{
						result = this.getMessage().compareTo(other.getMessage());
					}
				}
			}
		}
		else
		{
			// if other == null then return -1
			result = -1;
		}
		return result;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		if (this.hashCode == -1)
		{
			this.hashCode = Objects.hash(
					Long.valueOf(this.getMillis()),
					this.getMessage(),
					this.sessionId,
					this.getLevel());
		}
		return this.hashCode;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object other)
	{
		if (other == this)
		{
			return true;
		}
		else if (other == null)
		{
			return false;
		}
		else if (!(other instanceof LogMessage))
		{
			return false;
		}
		else
		{
			final LogMessage otherMessage = (LogMessage) other;
			return this.getMillis() == otherMessage.getMillis()
					&& this.getMessage().equals(otherMessage.getMessage())
					&& Objects.equals(this.sessionId, otherMessage.sessionId)// use Objects.equals as sessionId can be null
					&& this.getLevel().equals(otherMessage.getLevel());
		}
	}
}
