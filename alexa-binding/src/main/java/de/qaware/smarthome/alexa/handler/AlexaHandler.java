package de.qaware.smarthome.alexa.handler;

import static de.qaware.smarthome.alexa.AlexaBindingConstants.*;

import java.io.IOException;
import java.net.URI;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.qaware.smarthome.alexa.websocket.WebSocketHandler;
import de.qaware.smarthome.alexa.websocket.protocol.ChangeColorMessage;
import de.qaware.smarthome.alexa.websocket.protocol.Message;
import de.qaware.smarthome.alexa.websocket.protocol.OffMessage;
import de.qaware.smarthome.alexa.websocket.protocol.OnMessage;
import de.qaware.smarthome.alexa.websocket.protocol.RegisterMessage;

/**
 * The {@link AlexaHandler} is responsible for establishing a websocket connection and for sending and receiving
 * messages via this connection. Certain messages will cause to post events via the {@link EventPublisher}.
 *
 * @author Andreas Mayer
 */
public class AlexaHandler extends BaseThingHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlexaHandler.class);
    private static final int PING_DELAY_MS = 30000;

    private final EventPublisher eventPublisher;

    private String webSocketAddress;
    private String amazonEchoId;
    private WebSocketClient client;
    private boolean isDisposing;

    private Thread pingThread;
    private boolean pingThreadRunning;

    /**
     * Default-Constructor
     *
     * @param thing the thing
     * @param eventPublisher the event publisher
     */
    public AlexaHandler(Thing thing, EventPublisher eventPublisher) {
        super(thing);
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // do nothing
    }

    @Override
    public void initialize() {
        isDisposing = false;
        webSocketAddress = (String) getConfig().get(CONFIG_WEBSOCKET_ADDRESS);
        amazonEchoId = (String) getConfig().get(CONFIG_AMAZON_ECHO_ID);

        if (webSocketAddress == null || amazonEchoId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING);
        } else {
            connect();
        }
    }

    @Override
    public void dispose() {
        isDisposing = true;
        stopPingThread();
        disconnect();
    }

    /**
     * To create the websocket handler and connect to the configured websocket server.
     */
    private void connect() {
        WebSocketHandler handler = new WebSocketHandler();
        handler.setOnCloseHandler(() -> {
            updateStatus(ThingStatus.OFFLINE);
            stopPingThread();
            LOGGER.info("Websocket closed");
            if (!isDisposing) {
                LOGGER.info("Reconnecting");
                reconnectClient(handler);
            }
        });
        handler.setOnErrorHandler(e -> {
            updateStatus(ThingStatus.OFFLINE);
            stopPingThread();
            LOGGER.warn("Websocket exception, reconnecting", e);
            reconnectClient(handler);
        });
        handler.setOnConnectHandler(session -> {
            stopPingThread();
            startPingThread(handler, session);
            LOGGER.info("Websocket connected to {}", webSocketAddress);
            updateStatus(ThingStatus.ONLINE);
            try {
                handler.sendMessage(new RegisterMessage(amazonEchoId), session);
            } catch (IOException e) {
                LOGGER.warn("Exception while registering Echo ID", e);
            }
        });
        handler.setOnMessageHandler(this::handleWebSocketMessage);

        SslContextFactory factory = new SslContextFactory(true);
        client = new WebSocketClient(factory);

        connectClient(handler);
    }

    /**
     * To reconnect to the configured websocket server.
     *
     * @param handler The current websocket handler
     */
    private void reconnectClient(WebSocketHandler handler) {
        try {
            client.stop();
            connectClient(handler);
        } catch (Exception e) {
            LOGGER.error("Exception while reconnecting", e);
        }
    }

    /**
     * To disconnect from the connected websocket server.
     */
    private void disconnect() {
        LOGGER.info("Disconnecting websocket");
        try {
            client.stop();
        } catch (Exception e) {
            LOGGER.error("Exception while stopping client", e);
        }
        client = null;
    }

    /**
     * To connect to the configured websocket server.
     *
     * @param handler The current websocket handler
     */
    private void connectClient(WebSocketHandler handler) {
        try {
            client.start();
            URI uri = new URI(webSocketAddress);
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            client.connect(handler, uri, request);
        } catch (Exception e) {
            LOGGER.error("Exception while starting the websocket client", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    /**
     * To start a ping thread that monitors the websocket connection.
     *
     * @param handler The current websocket handler
     * @param session The current websocket session
     */
    private void startPingThread(WebSocketHandler handler, Session session) {
        if (pingThread != null) {
            throw new IllegalStateException("There is already a ping thread running");
        }

        LOGGER.info("Starting ping thread");
        pingThread = new Thread(() -> pingThreadMain(handler, session), "WebsocketPing");
        pingThreadRunning = true;
        pingThread.start();
    }

    /**
     * To stop the ping thread.
     */
    private void stopPingThread() {
        if (pingThread == null) {
            return;
        }

        LOGGER.info("Stopping ping thread");
        pingThreadRunning = false;
        pingThread.interrupt();
        try {
            pingThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        pingThread = null;
    }

    /**
     * Runtime logic for the ping thread.
     *
     * @param handler The current websocket handler
     * @param session The current websocket session
     */
    private void pingThreadMain(WebSocketHandler handler, Session session) {
        LOGGER.info("Ping thread started");
        while (pingThreadRunning) {
            try {
                LOGGER.debug("Pinging...");
                handler.sendPing(session);
            } catch (IOException e) {
                LOGGER.warn("Exception while sending ping");
            }
            try {
                Thread.sleep(PING_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        LOGGER.info("Ping thread ended");
    }

    /**
     * To handle a received message.
     *
     * @param message The message from the websocket server
     */
    private void handleWebSocketMessage(Message message) {
        LOGGER.info("Received WebSocket message: {}", message);

        switch (message.getType()) {
            case ON:
                handleOnMessage((OnMessage) message);
                break;
            case OFF:
                handleOffMessage((OffMessage) message);
                break;
            case CHANGE_COLOR:
                handleChangeColorMessage((ChangeColorMessage) message);
                break;
            default:
                LOGGER.error("Unknown message type: {}", message.getType());
                break;
        }
    }

    /**
     * To turn on the philips hue light
     *
     * @param message An {@link OnMessage}
     */
    private void handleOnMessage(OnMessage message) {
        LOGGER.info("Handling on message for thing {}", message.getThing());
        eventPublisher.post(ItemEventFactory.createCommandEvent(HUE_LIGHT_ID, OnOffType.ON));
    }

    /**
     * To turn off the philips hue light
     *
     * @param message An {@link OffMessage}
     */
    private void handleOffMessage(OffMessage message) {
        LOGGER.info("Handling off message for thing {}", message.getThing());
        eventPublisher.post(ItemEventFactory.createCommandEvent(HUE_LIGHT_ID, OnOffType.OFF));
    }

    /**
     * To change the color of the philips hue light
     *
     * @param message A {@link ChangeColorMessage}
     */
    private void handleChangeColorMessage(ChangeColorMessage message) {
        LOGGER.info("Handling change color message for thing {}, changing color to {}", message.getThing(),
                message.getColor() + ", rgb=[" + message.getRedProportion() + ',' + message.getGreenProportion() + ','
                        + message.getBlueProportion() + "]");
        eventPublisher.post(ItemEventFactory.createCommandEvent(HUE_LIGHT_ID, HSBType
                .fromRGB(message.getRedProportion(), message.getGreenProportion(), message.getBlueProportion())));
    }

}
