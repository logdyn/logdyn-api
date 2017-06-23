package com.logdyn.api.intergrations.java;

import com.logdyn.api.JsLevel;
import com.logdyn.api.intergrations.LogFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Created by Matt on 23/06/2017.
 */
public class JSONFormatter extends Formatter implements LogFormatter<LogRecord>
{
    /** The level label used for JSON Objects */
    private static final String LEVEL_LABEL = "level";
    /** The message label used for JSON Objects */
    private static final String MESSAGE_LABEL = "message";
    /** The timestamp label used for JSON Objects */
    private static final String TIMESTAMP_LABEL = "timestamp";


    private static final String LEVEL_NAME = "name";

    private static final String LEVEL_INT = "intValue";

    private static final String LEVEL_LOCAL = "localName";

    private static final Level DEFAULT_LEVEL = Level.FINE;


    private static JSONObject toJSONObject(final LogRecord logRecord)
    {
        return new JSONObject()
                .put(LEVEL_LABEL, JSONFormatter.levelToJSON(logRecord.getLevel()))
                .put(MESSAGE_LABEL, logRecord.getMessage())
                .put(TIMESTAMP_LABEL, Long.valueOf(logRecord.getMillis()));
    }

    private static JSONObject levelToJSON(final Level level)
    {
        return new JSONObject()
                .put(LEVEL_NAME, level.getName())
                .put(LEVEL_INT, level.intValue())
                .put(LEVEL_LOCAL, level.getLocalizedName());
    }

    @Override
    public String format(LogRecord record)
    {
        return JSONFormatter.toJSONObject(record).toString();
    }

    @Override
    public String format(Collection<LogRecord> records)
    {
        final JSONArray jsonArray = new JSONArray();
        for (final LogRecord logRecord : records)
        {
            jsonArray.put(JSONFormatter.toJSONObject(logRecord));
        }
        return jsonArray.toString();
    }

    @Override
    public List<LogRecord> parse(final String string)
    {
        final char firstChar = string.charAt(0);
        final List<LogRecord> results;
        if (firstChar == '[') //is json array
        {
            final JSONArray jsonArray = new JSONArray(string);
            results = new ArrayList<>(jsonArray.length());
            for(final Object jsonObject : jsonArray)
            {
                if (jsonObject instanceof JSONObject)
                {
                    results.add(JSONFormatter.parse((JSONObject) jsonObject));
                }
            }
        }
        else if (firstChar == '{') // is json object
        {
            final JSONObject jsonObject = new JSONObject(string);
            results = Collections.singletonList(JSONFormatter.parse(jsonObject));
        }
        else
        {
            throw new JSONException("Unrecognised json format");
        }
        return results;
    }

    private static LogRecord parse(final JSONObject jsonObject)
    {
        final LogRecord logRecord =
                new LogRecord(parseLevel(jsonObject), jsonObject.getString(MESSAGE_LABEL));
        logRecord.setMillis(jsonObject.optLong(TIMESTAMP_LABEL, System.currentTimeMillis()));
        return logRecord;
    }

    private static Level parseLevel(final JSONObject jsonObject)
    {
        final Object levelObject = jsonObject.opt(LEVEL_LABEL);
        String levelValue = null;
        if (levelObject instanceof JSONObject)
        {
            final JSONObject jsonLevelObject = (JSONObject) levelObject;
            levelValue = jsonLevelObject.optString(LEVEL_NAME, null);
            if (null == levelValue)
            {
                 levelValue = jsonLevelObject.optString(LEVEL_INT, null);
                 if (null == levelValue)
                 {
                     levelValue = jsonLevelObject.optString(LEVEL_LOCAL, null);
                 }
            }
        }
        else if (levelObject instanceof String)
        {
            levelValue = (String) levelObject;
        }
        if (null == levelValue)
        {
            return DEFAULT_LEVEL;
        }
        else
        {
            return JsLevel.parse(levelValue);
        }
    }
}
