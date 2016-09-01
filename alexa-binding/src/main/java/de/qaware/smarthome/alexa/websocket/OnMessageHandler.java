package de.qaware.smarthome.alexa.websocket;

import de.qaware.smarthome.alexa.websocket.protocol.Message;

public interface OnMessageHandler {
    void onMessage(Message message);
}
