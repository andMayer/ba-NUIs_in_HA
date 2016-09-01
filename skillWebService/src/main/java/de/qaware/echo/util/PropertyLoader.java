package de.qaware.echo.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Helper class to load property files
 *
 * @author Andreas Mayer
 */
public class PropertyLoader {

	/**
	 * Loads all properties of the passed file into the System.Properties
	 *
	 * @param fileName the name of the property file
	 */
	public static void loadProperty(String fileName) {
		Properties props = new Properties();

		try (InputStream is = PropertyLoader.class.getClassLoader().getResourceAsStream(fileName)) {
			if(is!= null) {
				props.load(is);

				for(String key: props.stringPropertyNames()) {
					System.setProperty(key, props.getProperty(key));
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("Unable to load property file \"" + fileName + "\"");
		}
	}

}
