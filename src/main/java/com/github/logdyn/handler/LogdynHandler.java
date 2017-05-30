package com.github.logdyn.handler;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.github.logdyn.endpoints.LoggingEndpoint;
import com.github.logdyn.model.LogMessage;

public class LogdynHandler extends Handler
{

	@Override
	public void publish(LogRecord record)
	{
		if (record instanceof LogMessage)
		{
			LoggingEndpoint.log((LogMessage) record);			
		}
	}

	@Override
	public void flush()
	{
		//NOOP
	}

	@Override
	public void close() throws SecurityException
	{
		//NOOP
	}

}
