package de.qaware.echo.skill.qivicon;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import de.qaware.echo.speech.SpeechletWrapper;
import de.qaware.echo.util.ColorType;
import de.qaware.echo.websocket.QiviconWebSocketHandler;
import de.qaware.echo.websocket.protocol.ChangeColorMessage;
import de.qaware.echo.websocket.protocol.OffMessage;
import de.qaware.echo.websocket.protocol.OnMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Handles the requests that where send by Alexa,
 * tries to forward them to the QIVICON environment and
 * returns a proper response.
 *
 * @author Andreas Mayer
 */
public class QiviconSpeechlet implements Speechlet {

	private static final Logger LOG = LoggerFactory.getLogger(QiviconSpeechlet.class);
	private static final String INVOCATION_NAME = "Qyvycon";
	private static final String DEVICE_SLOT = "Device";
	private static final String COLOR_SLOT = "Color";

	private final SpeechletWrapper wrapper = new SpeechletWrapper();
	private final QiviconWebSocketHandler webSocketHandler;

	/**
	 * Default-Constructor
	 *
	 * @param webSocketHandler
	 *      The handler for the websocket connection
	 */
	public QiviconSpeechlet(QiviconWebSocketHandler webSocketHandler) {
		this.webSocketHandler = webSocketHandler;
	}

	@Override
	public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
		LOG.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
	}

	@Override
	public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
		LOG.info("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
		return getWelcomeMessage();
	}

	@Override
	public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
		LOG.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

		Intent intent = request.getIntent();
		String intentName = (intent != null) ? intent.getName() : "";

		switch (intentName) {
			case "TurnOnIntent":
				return handleTurnOnIntent(intent, session);
			case "TurnOffIntent":
				return handleTurnOffIntent(intent, session);
			case "ChangeColorIntent":
				return handleChangeColor(intent, session);
			case "GetEchoIdIntent":
				return handleGetEchoIdIntent(intent, session);

			case "AMAZON.HelpIntent":
				return handleHelpIntent();
			case "AMAZON.StopIntent":
				return wrapper.newTellResponse("Goodbye");

			default:
				throw new SpeechletException("Invalid Intent");
		}
	}

	@Override
	public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
		LOG.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
	}

	/**
	 * To turn on a specific device.
	 *
	 * @param intent the intent request to handle
	 * @param session the session associated with the request
	 * @return {@link SpeechletResponse} the speechlet response
	 */
	private SpeechletResponse handleTurnOnIntent(Intent intent, Session session) {
		LOG.info("Handling turn on intent {}", intent);
		Slot deviceSlot = intent.getSlot(DEVICE_SLOT);

		if (deviceSlot != null && deviceSlot.getValue() != null) {
			String deviceName = deviceSlot.getValue();
			String echoId = session.getUser().getUserId();

			try {
				if(!webSocketHandler.sendToBinding(echoId, new OnMessage(deviceName))) {
					return wrapper.newTellResponse("Failed to turn on " + deviceName +
							". You should check out the configuration of your Echo ID in your " + INVOCATION_NAME + " environment");
				}
			} catch (IOException e) {
				LOG.warn("IOException while broadcasting", e);
				return wrapper.newTellResponse("There was an error while broadcasting your command. Please try again.");
			}
			return wrapper.newTellResponse("Turned on " + deviceName);
		}

		String repromt = "Now, what do you want to do?";
		String outputText = "I'm not sure which device you meant. " +
				" To turn on any device, say for example: turn on lamp." + repromt;
		return wrapper.newAskResponse(outputText, repromt);
	}

	/**
	 * To turn off a specific device.
	 *
	 * @param intent the intent request to handle
	 * @param session the session associated with the request
	 * @return {@link SpeechletResponse} the speechlet response
	 */
	private SpeechletResponse handleTurnOffIntent(Intent intent, Session session) {
		LOG.info("Handling turn off intent {}", intent);
		Slot deviceSlot = intent.getSlot(DEVICE_SLOT);

		if (deviceSlot != null && deviceSlot.getValue() != null) {
			String deviceName = deviceSlot.getValue();
			String echoId = session.getUser().getUserId();

			try {
				if(!webSocketHandler.sendToBinding(echoId, new OffMessage(deviceName))) {
					return wrapper.newTellResponse("Failed to turn off " + deviceName +
							". You should check out the configuration of your Echo ID in your " + INVOCATION_NAME + " environment");
				}
			} catch (IOException e) {
				LOG.warn("IOException while broadcasting", e);
				return wrapper.newTellResponse("There was an error while broadcasting your command. Please try again.");
			}
			return wrapper.newTellResponse("Turned off " + deviceName);
		}

		String repromt = "Now, what do you want to do?";
		String outputText = "I'm not sure which device you meant. " +
				" To turn off any device, say for example: turn off lamp." + repromt;
		return wrapper.newAskResponse(outputText, repromt);
	}

	/**
	 * To change the color of a specific device.
	 *
	 * @param intent the intent request to handle
	 * @param session the session associated with the request
	 * @return {@link SpeechletResponse} the speechlet response
	 */
	private SpeechletResponse handleChangeColor(Intent intent, Session session) {
		LOG.info("Handling change color intent {}", intent);
		Slot deviceSlot = intent.getSlot(DEVICE_SLOT);

		if (deviceSlot != null) {
			String deviceName = deviceSlot.getValue();
			if(deviceName == null) {
				Object deviceFromSession = session.getAttribute(DEVICE_SLOT);
				if(deviceFromSession != null) {
					deviceName = (String) deviceFromSession;
				} else {
					return wrapper.newTellResponse("I'm not sure which device you meant. " +
							" To change the color of your light, say for example: change the color of lamp to red.");
				}
			}

			Slot colorSlot = intent.getSlot(COLOR_SLOT);
			if(colorSlot != null && colorSlot.getValue() != null) {
				String color = colorSlot.getValue();
				ColorType colorType = ColorType.getColorType(color);

				try {
					switch (colorType) {
						case RED:
						case GREEN:
						case BLUE:
						case WHITE:
							String echoId = session.getUser().getUserId();
							LOG.info("Color identified as: {}, rgb values are [{},{},{}]", colorType.name(), colorType.getRed(), colorType.getGreen(), colorType.getBlue());
							if(!webSocketHandler.sendToBinding(echoId, new ChangeColorMessage(deviceName, colorType.name(), colorType.getRed(), colorType.getGreen(), colorType.getBlue()))) {
								return wrapper.newTellResponse("Failed to change the color of " + deviceName + " to " + color +
										". You should check out the configuration of your Echo ID in your " + INVOCATION_NAME + " environment");
							}
							break;
						default:
							session.setAttribute(DEVICE_SLOT, deviceName);
							return wrapper.newAskResponse("I'm not sure which color you meant. But you can choose between white, red, green and blue. So which color do you want?",
							"Which color do you want?");
					}
				} catch (IOException e) {
					LOG.warn("IOException while broadcasting", e);
					return wrapper.newTellResponse("There was an error while broadcasting your command. Please try again.");
				}

				return wrapper.newTellResponse("Changed the color of " + deviceName + " to " + color + ".");
			}
		}

		String repromt = "Now, what do you want to do?";
		String outputText = "I'm not sure which device you meant. " +
				" To change the color of your light, say for example: change the color of lamp to red." + repromt;
		return wrapper.newAskResponse(outputText, repromt);
	}

	/**
	 * To return the Amazon Echo ID for the user.
	 *
	 * @param intent the intent request to handle
	 * @param session the session associated with the request
	 * @return {@link SpeechletResponse} the speechlet response
	 */
	private SpeechletResponse handleGetEchoIdIntent(Intent intent, Session session) {
		LOG.info("Handling GetEchoIdIntent {}", intent);
		String outputText = "Your Amazon Echo ID was send to your Amazon Alexa App";
		String cardTitle = "Your Amazon Echo ID";
		String cardContent = "Amazon Echo ID: \"" + session.getUser().getUserId() + "\"\n" +
				"You can add this Amazon Echo ID in your QIVICON environment.\n" +
				"Hint: This ID may change each time you remove and add this skill to your Amazon Account.";
		return wrapper.newTellWithCardResponse(outputText, false, cardTitle, cardContent);
	}

	/**
	 * To tell the user how to use this skill.
	 *
	 * @return {@link SpeechletResponse} the speechlet response
	 */
	private SpeechletResponse handleHelpIntent() {
		LOG.info("Sending help message");
		String outputText = "With " + INVOCATION_NAME + " you can control the philips hue light in your " + INVOCATION_NAME + " environment. " +
				"For example you can say, turn on or turn off the light, or change the color of light to blue. " +
				" Now, what do u want to do?";
		String repromt = "What do you want to do?";
		return wrapper.newAskResponse(outputText, repromt);
	}

	/**
	 * To return a welcome message to the user.
	 *
	 * @return {@link SpeechletResponse} the speechlet response
	 */
	private SpeechletResponse getWelcomeMessage() {
		LOG.info("Sending welcome message.");
		String outputText = "Welcome to the " + INVOCATION_NAME + "skill. " +
				"With this skill, you can control the philips hue light in your " + INVOCATION_NAME + " environment. " +
				"For example you can say, turn on or turn off the light, or change the color of light to blue. " +
				"Now, what do u want to do?";
		String repromt = "With this skill, you can control the philips hue light in your " + INVOCATION_NAME + " environment. " +
				"For example you can say, turn on or turn off the light, or change the color of light to blue. " +
				"Now, what do u want to do?";
		return wrapper.newAskResponse(outputText, repromt);
	}

}
