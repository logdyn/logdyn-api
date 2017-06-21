package com.logdyn.api;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LogdynHandler extends Handler
{

	@Override
	public void publish(LogRecord record)
	{
		Logger.log(record);
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
