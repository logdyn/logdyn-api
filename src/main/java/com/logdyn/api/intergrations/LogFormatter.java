package com.logdyn.api.intergrations;

import java.util.Collection;
import java.util.List;

/**
 * Created by Matt on 23/06/2017.
 */
public interface LogFormatter<R>
{
    String format(final R record);

    String format(final Collection<R> records);

    List<R> parse(final String string);
}
