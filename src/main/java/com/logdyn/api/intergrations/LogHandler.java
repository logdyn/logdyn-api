package com.logdyn.api.intergrations;

/**
 * Created by Matt on 23/06/2017.
 */
public interface LogHandler<R>
{
    void publish(final R record);
}
