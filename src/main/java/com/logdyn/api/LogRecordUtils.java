package com.logdyn.api;

import java.util.Collection;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Matt on 11/06/2017.
 */
public class LogRecordUtils
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

    private static JSONObject toJSONObject(final LogRecord logRecord)
    {
        return new JSONObject()
                .put(LogRecordUtils.LEVEL_LABEL, logRecord.getLevel().getName())
                .put(LogRecordUtils.MESSAGE_LABEL, logRecord.getMessage())
                .put(LogRecordUtils.TIMESTAMP_LABEL, Long.valueOf(logRecord.getMillis()));
    }

    public static String toJSON(final LogRecord logRecord)
    {
        return LogRecordUtils.toJSONObject(logRecord).toString();
    }

    public static String toJSON(final Collection<LogRecord> logRecords)
    {
        final JSONArray jsonArray = new JSONArray();
        for (final LogRecord logRecord : logRecords)
        {
            jsonArray.put(LogRecordUtils.toJSONObject(logRecord));
        }
        return jsonArray.toString();
    }

    public static LogRecord fromJSON(final JSONObject jsonObject)
    {
        return LogRecordUtils.fromJSON(jsonObject, null, null);
    }

    public static LogRecord fromJSON(final JSONObject jsonObject, final String username, final String sessionId)
    {
        LogRecord record = new LogRecord(LogRecordUtils.parseLevel(jsonObject), jsonObject.getString(LogRecordUtils.MESSAGE_LABEL));
        record.setMillis(jsonObject.optLong(LogRecordUtils.TIMESTAMP_LABEL, System.currentTimeMillis()));
        return record;
    }

    private static Level parseLevel(final JSONObject jsonObject)
    {
        final String levelName = jsonObject.optString(LogRecordUtils.LEVEL_LABEL, null);
        if(null == levelName)
        {
            return LogRecordUtils.DEFAULT_LEVEL;
        }
        else
        {
            return JsLevel.parse(levelName);
        }
    }
}
