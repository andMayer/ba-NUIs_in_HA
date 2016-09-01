package de.qaware.echo.util;

/**
 * All supported colors with suitable rgb-values
 *
 * @author Andreas Mayer
 */
public enum ColorType {
	RED(255, 0, 0),
	GREEN(0, 255, 0),
	BLUE(0, 0, 255),
	WHITE(255, 255, 255),
	UNKOWN(-1, -1, -1);

	private final int red;
	private final int green;
	private final int blue;

	ColorType(int red, int green, int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public int getRed() {
		return red;
	}

	public int getGreen() {
		return green;
	}

	public int getBlue() {
		return blue;
	}

	public static ColorType getColorType(String colorName) {
		for (ColorType type : ColorType.values()) {
			if (type != UNKOWN && type.name().equalsIgnoreCase(colorName)) {
				return type;
			}
		}
		return UNKOWN;
	}

}
