package com.github.logdyn.model;

import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Java object containing message details: sessionId, level, message, timestamp
 * @author jsjlewis96
 */
public class LogMessage extends LogRecord implements Comparable<LogRecord>
{

	/** Generated serialID **/
	private static final long serialVersionUID = -9093297911420796177L;
	
	private static final String TIMESTAMP_LABEL = "timestamp";
	private static final String MESSAGE_LABEL = "message";
	private static final String LEVEL_LABEL = "level";
	private static final String SESSION_ID_LABEL = "sessionId";
	
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
	 * Takes a JSONObject
	 * @param jsonObject The JSONObject
	 * @throws JSONException
	 */
	public LogMessage(final JSONObject jsonObject) throws JSONException
	{
		this(jsonObject.optString(LogMessage.SESSION_ID_LABEL, null),
				LogMessage.parseLevel(jsonObject),
				jsonObject.optString(LogMessage.MESSAGE_LABEL, null),
				jsonObject.optLong(LogMessage.TIMESTAMP_LABEL, System.currentTimeMillis()));
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
	
	private static Level parseLevel(final JSONObject jsonObject)
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
	 * @return the sessionId
	 */
	public String getSessionId()
	{
		return this.sessionId;
	}

	@Override
	public int compareTo(LogRecord other)
	{
		return LogRecordComparitor.COMPARITOR.compare(this, other);
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
