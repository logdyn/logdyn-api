package com.logdyn.api.model;

import java.util.logging.Level;

import org.junit.Assert;
import org.junit.Test;

public class LogMessageTest
{
	@Test
	public void testEqualsObject()
	{
		final LogMessage msg1 = new LogMessage(null, Level.CONFIG,"TestMessage", 0);
		final LogMessage msg2 = new LogMessage(null, Level.CONFIG,"TestMessage", 0);
		Assert.assertEquals(msg1, msg2);
	}

	@Test
	public void testHashCode()
	{
		final LogMessage msg1 = new LogMessage(null, Level.CONFIG,"TestMessage", 0);
		final LogMessage msg2 = new LogMessage(null, Level.CONFIG,"TestMessage", 0);
		Assert.assertEquals(msg1.hashCode(), msg2.hashCode());
	}

	//TODO test compareTo method
}