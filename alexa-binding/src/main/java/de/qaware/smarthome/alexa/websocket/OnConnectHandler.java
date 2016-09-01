package de.qaware.smarthome.alexa.websocket;

import org.eclipse.jetty.websocket.api.Session;

public interface OnConnectHandler {
    void onConnect(Session session);
}
