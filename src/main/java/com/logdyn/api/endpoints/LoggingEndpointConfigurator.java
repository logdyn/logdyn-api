package com.logdyn.api.endpoints;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * Created by Matt on 09/06/2017.
 */
public class LoggingEndpointConfigurator extends ServerEndpointConfig.Configurator
{
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response)
    {
        sec.getUserProperties().put(HttpSession.class.getName(), request.getHttpSession());
        super.modifyHandshake(sec, request, response);
    }
}
