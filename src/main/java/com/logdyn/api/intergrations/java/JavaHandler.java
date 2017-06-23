package com.logdyn.api.intergrations.java;

import com.logdyn.api.intergrations.LogHandler;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by Matt on 23/06/2017.
 */
public class JavaHandler extends Handler implements LogHandler<LogRecord>
{
    @Override
    public void publish(final LogRecord record)
    {
        //TODO
    }

    @Override
    public void flush()
    {
        //NO-OP
    }

    @Override
    public void close() throws SecurityException
    {
        //NO-OP
    }
}
