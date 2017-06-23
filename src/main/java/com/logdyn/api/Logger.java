package com.logdyn.api;

import javax.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.LogRecord;

/**
 * Created by Matt on 21/06/2017.
 */
public class Logger
{
    private static final Map<String, LogSession> USER_SESSIONS = new ConcurrentHashMap<>();
    private static final Map<String, LogSession> NON_USER_SESSIONS = new ConcurrentHashMap<>();
    private static final LogSession ROOT_SESSION = new LogSession();

    private static final ThreadLocal<String> username = new ThreadLocal<>();
    private static final ThreadLocal<String> sessionId = new ThreadLocal<>();

    /**
     * Sends a LogRecord to the relevant Websockets and stores it for later websockets to use.
     * @param logRecord the LogRecord to log.
     * @return true if the logRecord was successfully stored.
     */
    public static boolean log (final LogRecord logRecord)
    {
        return Logger.log(logRecord, null);
    }

    /**
     * Sends a LogRecord to the relevant Websockets and stores it for later websockets to use.
     * @param logRecord the LogRecord to log.
     * @param exclude a session to exclude from sending the messages to.
     * @return true if the logRecord was successfully stored.
     */
    public static boolean log(final LogRecord logRecord, final Session exclude)
    {
        final String httpSessionId = Logger.getCurrentSessionId();
        final String username = Logger.getCurrentUsername();

        return Logger.getLogSession(username, httpSessionId).logMessage(logRecord, exclude);
    }

    /**
     * Clears out the LogSession for a particular user.
     * @param username the username of the LogSession to clear
     * @return true if a session was removed
     */
    public static boolean clearUser(final String username)
    {
        return Logger.USER_SESSIONS.remove(username) != null;
    }

    /**
     * Clears out the LogSession for a particular httpSession.
     * @param httpSessionId the httpSession Id of the LogSession to clear
     * @return true if a session was removed
     */
    public static boolean clearSession(final String httpSessionId)
    {
        return Logger.NON_USER_SESSIONS.remove(httpSessionId) != null;
    }

    /**
     * adds a session to the relevant LogSession.
     * @param session the session to add
     * @param username the username of the user using the session
     * @param httpSessionId the httpSessionId of the session that is using the session.
     */
    public static void addSession(final Session session, final String username, final String httpSessionId)
    {
        final LogSession logSession = getLogSession(username, httpSessionId);
        logSession.addWebsocketSession(session);
        if (logSession != Logger.ROOT_SESSION)
        {
            ROOT_SESSION.addWebsocketSession(session);
        }
        logSession.sendMessages(session, ROOT_SESSION);
    }

    /**
     * removes a session from the relevant LogSession.
     * @param session the session to remove
     * @param username the username of the user using the session
     * @param httpSessionId the httpSessionId of the session that is using the session.
     */
    public static void removeSession(final Session session, final String username, final String httpSessionId)
    {
        final LogSession logSession = getLogSession(username, httpSessionId);
        logSession.removeWebsocketSession(session);
        if (logSession != Logger.ROOT_SESSION)
        {
            Logger.ROOT_SESSION.removeWebsocketSession(session);
        }
    }

    /**
     * Gets the most relevant LogSession.
     *
     * first gets a session based on username, else trys to get one based on httpSessionId.
     * if all else fails gets the Root session shared by everyone.
     * @param username a username to get or create
     * @param httpSessionId a heepSessionId to get or create
     * @return the most relevant LogSession
     */
    private static LogSession getLogSession(final String username, final String httpSessionId)
    {
        LogSession result;
        if (null != username)
        {
            result = Logger.USER_SESSIONS.get(username);
            if (null == result)
            {
                result = new LogSession();
                Logger.USER_SESSIONS.put(username, result);
            }
        }
        else if (null != httpSessionId)
        {
            result = Logger.NON_USER_SESSIONS.get(httpSessionId);
            if (null == result)
            {
                result = new LogSession();
                Logger.NON_USER_SESSIONS.put(httpSessionId, result);
            }
        }
        else
        {
            result = Logger.ROOT_SESSION;
        }
        return result;
    }

    /**
     * removes current values for thread local variables.
     */
    static void clearThreadLocals()
    {
        Logger.username.remove();
        Logger.sessionId.remove();
    }

    /**
     * gets the username of the request currently being processed.
     * @return the username
     */
    public static String getCurrentUsername()
    {
        return Logger.username.get();
    }

    /**
     * gets the httpSessionId of the request currently being processed.
     * @return the session Id
     */
    public static String getCurrentSessionId()
    {
        return Logger.sessionId.get();
    }

    /**
     * sets the username of the request that is currently being processed.
     * @param username the username to set
     */
    static void setCurrentUsername(final String username)
    {
        Logger.username.set(username);
    }

    /**
     * sets the httpSessionId of the request that is currently being processed.
     * @param sessionId the session ID to set
     */
    static void setCurrentSessionId(final String sessionId)
    {
        Logger.sessionId.set(sessionId);
    }
}
