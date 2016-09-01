package de.qaware.smarthome.alexa.internal;

import static de.qaware.smarthome.alexa.AlexaBindingConstants.THING_TYPE_ALEXA;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

import de.qaware.smarthome.alexa.handler.AlexaHandler;;

/**
 * The {@link AlexaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Andreas Mayer
 */
public class AlexaHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_ALEXA);
    private EventPublisher eventPublisher;

    public AlexaHandlerFactory() {
        // do nothing
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_ALEXA)) {
            return new AlexaHandler(thing, eventPublisher);
        }

        return null;
    }
}
