package de.qaware.smarthome.alexa.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import de.qaware.smarthome.alexa.websocket.protocol.Message;

/**
 * To send and receivce messages via the websocket connections.
 *
 * @author Andreas Mayer
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class WebSocketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketHandler.class);

    private OnCloseHandler onCloseHandler;
    private OnConnectHandler onConnectHandler;
    private OnErrorHandler onErrorHandler;
    private OnMessageHandler onMessageHandler;

    private final Gson gson = new Gson();

    public void setOnCloseHandler(OnCloseHandler onCloseHandler) {
        this.onCloseHandler = onCloseHandler;
    }

    public void setOnConnectHandler(OnConnectHandler onConnectHandler) {
        this.onConnectHandler = onConnectHandler;
    }

    public void setOnErrorHandler(OnErrorHandler onErrorHandler) {
        this.onErrorHandler = onErrorHandler;
    }

    public void setOnMessageHandler(OnMessageHandler onMessageHandler) {
        this.onMessageHandler = onMessageHandler;
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        if (onErrorHandler != null) {
            onErrorHandler.onError(error);
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        if (onConnectHandler != null) {
            onConnectHandler.onConnect(session);
        }
    }

    @OnWebSocketMessage
    public void onMessage(String msg) throws IOException {
        if (onMessageHandler != null) {
            LOGGER.debug("Got message {} from websocket", msg);

            Message dummyMessage = gson.fromJson(msg, Message.class);
            if (dummyMessage.getType() == null) {
                LOGGER.error("Received unsupported message. Check that the binding version and server matches.");
                return;
            }
            Message message = gson.fromJson(msg, dummyMessage.getType().getMessageClass());

            onMessageHandler.onMessage(message);
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        if (onCloseHandler != null) {
            onCloseHandler.onClose();
        }
    }

    public void sendPing(Session session) throws IOException {
        ByteBuffer payload = ByteBuffer.wrap(new byte[] { 'A' });
        session.getRemote().sendPing(payload);
    }

    public void sendMessage(Message message, Session session) throws IOException {
        String json = gson.toJson(message);
        session.getRemote().sendString(json);
    }
}