package com.logdyn.api.intergrations;

import java.util.Collection;
import java.util.List;

/**
 * Created by Matt on 23/06/2017.
 */
public class ToStringFormatter<R> implements LogFormatter<R>
{
    @Override
    public String format(Object record)
    {
        return record.toString();
    }

    @Override
    public String format(Collection<R> records)
    {
        return records.toString();
    }

    @Override
    public List<R> parse(String string)
    {
        throw new UnsupportedOperationException();
    }
}
