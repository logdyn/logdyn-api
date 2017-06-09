package com.logdyn.api.handler;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.logdyn.api.endpoints.LoggingEndpoint;

public class LogdynHandler extends Handler
{

	@Override
	public void publish(LogRecord record)
	{
		LoggingEndpoint.log(record);
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