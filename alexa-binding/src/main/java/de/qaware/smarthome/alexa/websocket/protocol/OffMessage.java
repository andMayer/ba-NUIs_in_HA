package de.qaware.smarthome.alexa.websocket.protocol;

/**
 * To turn off a thing.
 *
 * @author Andreas Mayer
 */
public class OffMessage extends Message {
    private String thing;

    public OffMessage() {
        super(MessageType.OFF);
    }

    public OffMessage(String thing) {
        this();
        this.thing = thing;
    }

    public String getThing() {
        return thing;
    }

    public void setThing(String thing) {
        this.thing = thing;
    }

    @Override
    public String toString() {
        return "OffMessage{" + "thing='" + thing + '\'' + '}';
    }
}
