package de.qaware.echo.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.qaware.echo.websocket.protocol.Message;
import de.qaware.echo.websocket.protocol.RegisterMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * To send and receivce messages via the websocket connections.
 *
 * @author Andreas Mayer
 */
public class QiviconWebSocketHandler extends TextWebSocketHandler {

	private final static Logger LOGGER = LoggerFactory.getLogger(QiviconWebSocketHandler.class);

	private final static Map<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();
	private final static Map<String, String> USER_TO_SESSION_IDS = new ConcurrentHashMap<>();

	private final ObjectMapper objectMapper;

	@Autowired
	public QiviconWebSocketHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		LOGGER.info("Connection established: {}, id: {}", session.getRemoteAddress(), session.getId());
		SESSIONS.put(session.getId(), session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		LOGGER.info("Connection closed: {}, id: {}", session.getRemoteAddress(), session.getId());
		SESSIONS.remove(session.getId());
	}

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage payload) throws IOException {
		LOGGER.info("Received new message: {}, id: {}", session.getRemoteAddress(), session.getId());

		String json = payload.getPayload();

		Message dummyMessage = objectMapper.readValue(json, Message.class);
		if (dummyMessage.getType() == null) {
			LOGGER.error("Received unsupported message. Check that the binding version and server matches.");
			return;
		}
		Message message = objectMapper.readValue(json, dummyMessage.getType().getMessageClass());

		switch (message.getType()) {
			case REGISTER:
				handleRegisterMessage(session, (RegisterMessage) message);
				break;
			default:
				LOGGER.warn("Unknown message received: {}", json);
				break;
		}
	}

	/**
	 * To link a Amazon Echo ID to the corresponding session.
	 *
	 * @param session The session that send this message
	 * @param message The RegisterMessage
	 */
	private void handleRegisterMessage(WebSocketSession session, RegisterMessage message) {
		LOGGER.info("Register echoID: {}, id: {}", message.getAmazonEchoId(), session.getId());
		USER_TO_SESSION_IDS.put(message.getAmazonEchoId(), session.getId());
	}

	/**
	 * To send a message to a binding which is linked to the amazonEchoId
	 *
	 * @param amazonEchoId
	 *      The id of the amazon echo that wants to send the message
	 * @param message
	 *      The message to be send
	 * @return true if the message was successfully sent, false else
	 * @throws IOException If serialization fails
	 */
	public boolean sendToBinding(String amazonEchoId, Message message) throws IOException {
		String sessionId = USER_TO_SESSION_IDS.get(amazonEchoId);
		if (sessionId == null) {
			LOGGER.warn("Tried to send to {}, but no session is registered", amazonEchoId);
			return false;
		}

		WebSocketSession session = SESSIONS.get(sessionId);
		if (session == null) {
			LOGGER.warn("Tried to send to {}, but no session is registered", amazonEchoId);
			return false;
		}

		String json = objectMapper.writeValueAsString(message);
		TextMessage webSocketMessage = new TextMessage(json);

		boolean success = send(session, webSocketMessage);
		if (!success) {
			// Close session, send failed
			cleanupSession(session.getId());
			close(session);
			SESSIONS.remove(sessionId);
		}
		return success;
	}

	/**
	 * Sends a message to a specific session.
	 *
	 * @param session
	 *      The target session
	 * @param message
	 *      The message to be send
	 * @return true if successful, false else
	 */
	private boolean send(WebSocketSession session, TextMessage message) {
		try {
			session.sendMessage(message);
		} catch (IOException e) {
			LOGGER.warn("IOException while sending websocket message to {}", session.getRemoteAddress());
			return false;
		}
		return true;
	}

	/**
	 * Removes a specific session.
	 *
	 * @param sessionId
	 *      The id of the session that will be removed
	 */
	private void cleanupSession(String sessionId) {
		USER_TO_SESSION_IDS.values().remove(sessionId);
	}

	/**
	 * To close the connection with a specific session.
	 *
	 * @param session
	 *      The session that will be closed
	 */
	private void close(WebSocketSession session) {
		try {
			session.close();
		} catch (IOException e) {
			// do nothing
		}
	}
}