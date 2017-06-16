package com.logdyn.api.handler;

import com.logdyn.api.endpoints.LoggingEndpoint;
import com.logdyn.api.model.JsonFormatter;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LogdynHandler extends Handler
{

	public LogdynHandler()
	{
		this.setFormatter(new JsonFormatter());
	}

	@Override
	public void publish(LogRecord record)
	{
		LoggingEndpoint.log(record, this.getFormatter());
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
