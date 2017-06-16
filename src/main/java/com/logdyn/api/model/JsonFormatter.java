package com.logdyn.api.model;

import org.json.JSONObject;

import java.util.Collection;
import java.util.Objects;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Created by Matt on 16/06/2017.
 */
public class JsonFormatter extends Formatter
{
    /** The level label used for JSON Objects */
    private static final String LEVEL_LABEL = "level";
    /** The message label used for JSON Objects */
    private static final String MESSAGE_LABEL = "message";

    private static final String USERNAME_LABEL = "username";

    private static final String SESSION_ID_LABEL = "sessionId";
    /** The timestamp label used for JSON Objects */
    private static final String TIMESTAMP_LABEL = "timestamp";

    private static final Level DEFAULT_LEVEL = Level.FINE;

    private static final char HEAD = '[';

    private static final char TAIL = ']';

    @Override
    public String getHead(Handler h)
    {
        return String.valueOf(HEAD);
    }

    @Override
    public String getTail(Handler h)
    {
        return String.valueOf(TAIL);
    }

    @Override
    public String format(LogRecord record)
    {
        return this.toJSONObject(record).toString();
    }

    private JSONObject toJSONObject(final LogRecord logRecord)
    {
        return new JSONObject()
                .put(JsonFormatter.LEVEL_LABEL, logRecord.getLevel().getName())
                .put(JsonFormatter.MESSAGE_LABEL, logRecord.getMessage())
                .put(JsonFormatter.TIMESTAMP_LABEL, Long.valueOf(logRecord.getMillis()));
    }

    public String format(final Collection<LogRecord> logRecords)
    {
        final StringBuilder result = new StringBuilder(logRecords.size() * 20);
        result.append(HEAD);
        for (final LogRecord logRecord : logRecords)
        {
            result.append(this.toJSONObject(logRecord));
            result.append(',');
        }
        result.setCharAt(result.length() - 1, TAIL);
        return result.toString();
    }

    public static LogRecord fromJSON(final JSONObject jsonObject)
    {
        return JsonFormatter.fromJSON(jsonObject, null, null);
    }

    public static LogRecord fromJSON(final JSONObject jsonObject, final String username, final String sessionId)
    {
        return new LogMessage(
                JsonFormatter.parseLevel(jsonObject),
                jsonObject.getString(JsonFormatter.MESSAGE_LABEL),
                Objects.toString(username, jsonObject.optString(JsonFormatter.USERNAME_LABEL, null)),
                Objects.toString(sessionId, jsonObject.optString(JsonFormatter.SESSION_ID_LABEL, null)),
                jsonObject.optLong(JsonFormatter.TIMESTAMP_LABEL, System.currentTimeMillis()));
    }

    private static Level parseLevel(final JSONObject jsonObject)
    {
        final String levelName = jsonObject.optString(JsonFormatter.LEVEL_LABEL, null);
        if(null == levelName)
        {
            return JsonFormatter.DEFAULT_LEVEL;
        }
        else
        {
            return JsLevel.parse(levelName);
        }
    }
}
