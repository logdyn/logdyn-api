package com.logdyn.api.intergrations.java;

import com.logdyn.api.intergrations.LogFormatter;

import java.util.Collection;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Created by Matt on 23/06/2017.
 */
public class FormatterAdapter extends Formatter implements LogFormatter<LogRecord>
{
    final Formatter formatter;

    public FormatterAdapter(final Formatter formatter)
    {
        this.formatter = formatter;
    }

    @Override
    public String format(final LogRecord record)
    {
        return this.formatter.format(record);
    }

    @Override
    public String format(Collection<LogRecord> records)
    {
        final StringBuilder builder = new StringBuilder(records.size() * 16);
        for (final LogRecord logRecord : records)
        {
            builder.append(logRecord);
            builder.append("\r\n");
        }
        return builder.toString();
    }

    @Override
    public List<LogRecord> parse(String string)
    {
        throw new UnsupportedOperationException();
    }
}
