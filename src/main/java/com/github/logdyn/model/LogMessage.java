package com.github.logdyn.model;

import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

/**
 * Java object containing message details: sessionId, level, message, timestamp
 * @author jsjlewis96
 */
public class LogMessage implements Comparable<LogMessage>, JSONString
{
	private static final String TIMESTAMP_LABEL = "timestamp";
	private static final String MESSAGE_LABEL = "message";
	private static final String LEVEL_LABEL = "level";
	private static final String SESSION_ID_LABEL = "sessionId";
	
	private static final Level DEFAULT_LEVEL = Level.FINE;
	
	private final String sessionId;
	private final Level level;
	private final String message;
	private final long timestamp;
	
	private String jsonString;
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
	
	public LogMessage(final JSONObject jsonObject) throws JSONException
	{
		//get timestamp first in order to have most accurate time.
		this.timestamp = jsonObject.optLong(LogMessage.TIMESTAMP_LABEL, System.currentTimeMillis());
		this.sessionId = jsonObject.optString(LogMessage.SESSION_ID_LABEL, null);
		this.level = LogMessage.parseLevel(jsonObject);
	}
	
	private static final Level parseLevel(final JSONObject jsonObject)
	{
		final String levelName = jsonObject.optString(LogMessage.LEVEL_LABEL);
		switch (levelName)
		{
			case "":
				return LogMessage.DEFAULT_LEVEL;
			case "ERROR": //map javascript error names on to java Levels
				return Level.SEVERE;
			case "WARN":
				return Level.WARNING;
			default:
				return Level.parse(levelName);
		}
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
		this.sessionId = sessionId;
		this.level = level;
		this.message = message;
		this.timestamp = timestamp;
	}

	/**
	 * @return the sessionId
	 */
	public String getSessionId()
	{
		return this.sessionId;
	}

	/**
	 * @return the level
	 */
	public Level getLevel()
	{
		return this.level;
	}

	/**
	 * @return the message
	 */
	public String getMessage()
	{
		return this.message;
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp()
	{
		return this.timestamp;
	}

	@Override
	public int compareTo(final LogMessage other)
	{
		return new CompareToBuilder()
			.append(this.timestamp, other.timestamp)
			.append(this.level.intValue(), other.level.intValue())
			.append(this.message, other.message)
			.toComparison();
	}

	@Override
	public String toJSONString()
	{
		if (this.jsonString == null)
		{
			this.jsonString = new JSONObject()
				.put(LogMessage.SESSION_ID_LABEL, this.sessionId)
				.put(LogMessage.LEVEL_LABEL, this.level.getName())
				.put(LogMessage.MESSAGE_LABEL, this.message)
				.put(LogMessage.TIMESTAMP_LABEL, Long.valueOf(this.timestamp))
				.toString();
		}
		return this.jsonString;
	}
	
	@Override
	public String toString()
	{
		return this.toJSONString();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		if (this.hashCode == -1)
		{
			this.hashCode = new HashCodeBuilder()
				.appendSuper(super.hashCode())
				.append(this.timestamp)
				.append(this.message)
				.append(this.sessionId)
				.append(this.level)
				.toHashCode();
		}
		return this.hashCode;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object other)
	{
		if (other instanceof LogMessage)
		{
			final LogMessage otherMessage = (LogMessage) other;
			return new EqualsBuilder()
				.appendSuper(super.equals(other))
				.append(this.timestamp, otherMessage.timestamp)
				.append(this.message, otherMessage.message)
				.append(this.sessionId, otherMessage.sessionId)
				.append(this.level, otherMessage.level)
				.isEquals();
		}
		else
		{
			return false;
		}
	}
}
