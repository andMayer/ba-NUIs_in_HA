package de.qaware.smarthome.alexa.websocket.protocol;

/**
 * To register the Amazon Echo ID at the webserver.
 *
 * @author Andreas Mayer
 */
public class RegisterMessage extends Message {
    private String amazonEchoId;

    public RegisterMessage() {
        super(MessageType.REGISTER);
    }

    public RegisterMessage(String amazonEchoId) {
        this();
        this.amazonEchoId = amazonEchoId;
    }

    public String getAmazonEchoId() {
        return amazonEchoId;
    }

    public void setAmazonEchoId(String amazonEchoId) {
        this.amazonEchoId = amazonEchoId;
    }

    @Override
    public String toString() {
        return "RegisterMessage{" + "amazonEchoId='" + amazonEchoId + '\'' + '}';
    }
}
