package de.qaware.echo.websocket.protocol;

/**
 * To change the color of a thing.
 *
 * @author Andreas Mayer
 */
public class ChangeColorMessage extends Message {
    private String thing;
    private String color;
    private int redProportion;
    private int greenProportion;
    private int blueProportion;

    public ChangeColorMessage() {
        super(MessageType.CHANGE_COLOR);
    }

    public ChangeColorMessage(String thing, String color, int r, int g, int b) {
        this();
        this.thing = thing;
        this.color = color;
        redProportion = r;
        greenProportion = g;
        blueProportion = b;
    }

    public String getThing() {
        return thing;
    }

    public void setThing(String thing) {
        this.thing = thing;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getRedProportion() {
        return redProportion;
    }

    public void setRedProportion(int redProportion) {
        this.redProportion = redProportion;
    }

    public int getGreenProportion() {
        return greenProportion;
    }

    public void setGreenProportion(int greenProportion) {
        this.greenProportion = greenProportion;
    }

    public int getBlueProportion() {
        return blueProportion;
    }

    public void setBlueProportion(int blueProportion) {
        this.blueProportion = blueProportion;
    }

    @Override
    public String toString() {
        return "ChangeColorMessage{" + "thing='" + thing + '\'' + ", color='" + color + "\', " +
                "rgb-value=[" + redProportion + ','  + greenProportion + ',' + blueProportion + "]}";
    }
}
