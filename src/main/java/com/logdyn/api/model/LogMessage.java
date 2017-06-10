package com.logdyn.api.model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
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
	
	private final String sessionId;
	private final String username;
	private int hashCode = -1;

	public LogMessage (final Level level, final String message)
	{
		this(level, message, (String) null, null);
	}

	public LogMessage (final Level level, final String message,
					   final HttpServletRequest request)
	{
		this(level, message, request, System.currentTimeMillis());
	}

	public LogMessage (final Level level, final String message,
					   final Principal userPrinciple, final HttpSession session)
	{
		this(level, message, userPrinciple, session, System.currentTimeMillis());
	}

	public LogMessage (final Level level, final String message,
					   final String username, final String sessionId)
	{
		this(level, message, username, sessionId, System.currentTimeMillis());
	}
	
	public LogMessage (final Level level, final String message,
					   final HttpServletRequest request, final long timestamp)
	{
		this(level, message,
				null != request ? request.getUserPrincipal() : null,
				null != request ? request.getSession(false) : null,
				timestamp);
	}

	public LogMessage (final Level level, final String message,
					   final Principal userPrinciple, final HttpSession session,
					   final long timestamp)
	{
		this(level, message,
				null != userPrinciple ? userPrinciple.getName() : null,
				null != session ? session.getId() : null,
				timestamp);
	}

	public LogMessage (final Level level, final String message,
					   final String username, final String sessionId,
					   final long timestamp)
	{
		super(level, message);
		this.setMillis(timestamp);
		this.username = username;
		this.sessionId = sessionId;
	}

	public String getUsername()
	{
		return this.username;
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
		return LogRecordComparator.compareTo(this, other);
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
					this.username,
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
					&& Objects.equals(this.username, otherMessage.username)// use Objects.equals as username can be null
					&& Objects.equals(this.sessionId, otherMessage.sessionId)// use Objects.equals as sessionId can be null
					&& this.getLevel().equals(otherMessage.getLevel());
		}
	}
}
