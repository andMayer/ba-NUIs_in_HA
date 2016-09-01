package de.qaware.smarthome.alexa;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link AlexaBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Andreas Mayer
 */
public class AlexaBindingConstants {

    public static final String BINDING_ID = "alexa";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_ALEXA = new ThingTypeUID(BINDING_ID, "alexa");

    // Light config properties
    public static final String HUE_LIGHT_ID = "hue__0017880a2b37_1_color";

    public static final String CONFIG_WEBSOCKET_ADDRESS = "websocket_address";

    public static final String CONFIG_AMAZON_ECHO_ID = "amazon_echo_id";

}
