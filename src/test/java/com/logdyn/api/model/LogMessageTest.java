package com.logdyn.api.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.logging.Level;

public class LogMessageTest
{
	@Test
	public void testEqualsObject()
	{
		final LogMessage msg1 = new LogMessage(Level.CONFIG,"TestMessage",null, 0);
		final LogMessage msg2 = new LogMessage(Level.CONFIG,"TestMessage",null, 0);
		Assert.assertEquals(msg1, msg2);
	}

	@Test
	public void testHashCode()
	{
		final LogMessage msg1 = new LogMessage(Level.CONFIG,"TestMessage",null, 0);
		final LogMessage msg2 = new LogMessage(Level.CONFIG,"TestMessage",null, 0);
		Assert.assertEquals(msg1.hashCode(), msg2.hashCode());
	}

	//TODO test compareTo method
}
