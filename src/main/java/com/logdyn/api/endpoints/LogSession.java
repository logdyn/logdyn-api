package com.logdyn.api.endpoints;

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.LogRecord;

import javax.websocket.Session;

import org.json.JSONObject;

import com.logdyn.api.model.LogMessage;
import com.logdyn.api.model.LogRecordComparator;

class LogSession
{
	private static final String TIMESTAMP_LABEL = "timestamp";
	private static final String MESSAGE_LABEL = "message";
	private static final String LEVEL_LABEL = "level";
	private static final String SESSION_ID_LABEL = "sessionId";
	
	private final Set<Session> sessions = Collections.newSetFromMap(new ConcurrentHashMap<Session, Boolean>());
	private final SortedSet<LogRecord> messages = new ConcurrentSkipListSet<>(LogRecordComparator.COMPARATOR);
    
	public boolean addWebsocketSession(final Session session)
    {
    	return this.sessions.add(session);
    }
    
    public boolean removeWebsocketSession(final Session session)
    {
    	return this.sessions.remove(session);
    }
    
    public boolean isSessionsEmpty()
    {
    	return this.sessions.isEmpty();
    }
    
    public void logMessage(final LogRecord logRecord)
    {
    	this.logMessage(logRecord, null);
    }
    
    public void logMessage(final LogRecord logRecord, final Session exclude)
    {
    	this.messages.add(logRecord);
    	
    	for (final Session session : sessions)
    	{
    		if (!session.equals(exclude))
    		{
    			session.getAsyncRemote().sendText(LogSession.logRecordToJSON(logRecord));
    		}
    	}
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
    
    private static String logRecordToJSON(final LogRecord logRecord)
	{
		return new JSONObject()
				.put(LogSession.SESSION_ID_LABEL, LogSession.getSessionId(logRecord))
				.put(LogSession.LEVEL_LABEL, logRecord.getLevel().getName())
				.put(LogSession.MESSAGE_LABEL, logRecord.getMessage())
				.put(LogSession.TIMESTAMP_LABEL, Long.valueOf(logRecord.getMillis()))
				.toString();
	}
}
