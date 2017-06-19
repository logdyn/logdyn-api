package com.logdyn.api;

import javax.servlet.http.HttpSession;
import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.Extension;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Matt on 16/06/2017.
 */
public class LoggingEndpointConfig implements ServerEndpointConfig
{
    private final String path;
    private final Map<String, Object> userParams = new HashMap<>();
    public LoggingEndpointConfig(final String path)
    {
        this.path = path;
    }

    @Override
    public Class<?> getEndpointClass()
    {
        return LoggingEndpoint.class;
    }

    @Override
    public String getPath()
    {
        return this.path;
    }

    @Override
    public List<String> getSubprotocols()
    {
        return Collections.emptyList();
    }

    @Override
    public List<Extension> getExtensions()
    {
        return Collections.emptyList();
    }

    @Override
    public Configurator getConfigurator()
    {
        return new ServerEndpointConfig.Configurator()
        {
            @Override
            public void modifyHandshake(final ServerEndpointConfig sec,
                                        final HandshakeRequest request, final HandshakeResponse response)
            {
                sec.getUserProperties().put(HttpSession.class.getName(), request.getHttpSession());
                super.modifyHandshake(sec, request, response);
            }
        };
    }

    @Override
    public List<Class<? extends Encoder>> getEncoders()
    {
        return Collections.emptyList();
    }

    @Override
    public List<Class<? extends Decoder>> getDecoders()
    {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> getUserProperties()
    {
        return this.userParams;
    }
}
