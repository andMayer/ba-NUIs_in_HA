package de.qaware.smarthome.alexa.websocket.protocol;

/**
 * To turn on a thing.
 *
 * @author Andreas Mayer
 */
public class OnMessage extends Message {
    private String thing;

    public OnMessage() {
        super(MessageType.ON);
    }

    public OnMessage(String thing) {
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
        return "OnMessage{" + "thing='" + thing + '\'' + '}';
    }
}
