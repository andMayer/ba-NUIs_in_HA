package de.qaware.smarthome.alexa.websocket.protocol;

/**
 * The type of a message.
 *
 * @author Andreas Mayer
 */
public enum MessageType {
    ON(OnMessage.class),
    OFF(OffMessage.class),
    CHANGE_COLOR(ChangeColorMessage.class),
    REGISTER(RegisterMessage.class);

    private final Class<? extends Message> messageClass;

    MessageType(Class<? extends Message> messageClass) {
        this.messageClass = messageClass;
    }

    public Class<? extends Message> getMessageClass() {
        return messageClass;
    }
}
