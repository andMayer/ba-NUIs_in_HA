package de.qaware.smarthome.alexa.websocket.protocol;

/**
 * Abstract base class for every message.
 *
 * @author Andreas Mayer
 */
public class Message {
    private MessageType type;

    public Message() {
    }

    public Message(MessageType type) {
        this.type = type;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}
