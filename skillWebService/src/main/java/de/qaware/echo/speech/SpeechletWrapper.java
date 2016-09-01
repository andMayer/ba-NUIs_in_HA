package de.qaware.echo.speech;

import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.*;

/**
 * Wrapper for creating various responses
 *
 * @author Andreas Mayer
 */
public class SpeechletWrapper {

	/**
	 * Wrapper for creating the Tell response from the input string.
	 *
	 * @param outputText
	 * 		the output to be spoken, only plain text (no SSML)
	 * @return {@link SpeechletResponse} the speechlet response
	 */
	public SpeechletResponse newTellResponse(String outputText) {
		return newTellResponse(outputText, false);
	}

	/**
	 * Wrapper for creating the Tell response from the input string
	 *
	 * @param outputText
	 * 		the output to be spoken
	 * @param isOutputSsml
	 * 		whether the output text is of type SSML
	 *
	 * @return {@link SpeechletResponse} the speechlet response
	 */
	public SpeechletResponse newTellResponse(String outputText, boolean isOutputSsml) {
		OutputSpeech outputSpeech;
		if (isOutputSsml) {
			outputSpeech = new SsmlOutputSpeech();
			((SsmlOutputSpeech) outputSpeech).setSsml(outputText);
		} else {
			outputSpeech = new PlainTextOutputSpeech();
			((PlainTextOutputSpeech) outputSpeech).setText(outputText);
		}
		return SpeechletResponse.newTellResponse(outputSpeech);
	}

	/**
	 * Wrapper for creating a Tell response that includes a card
	 *
	 * @param outputText
	 *      the output to be spoken
	 * @param isOutputSsml
	 *      whether the output text is of type SSML
	 * @param cardTitle
	 *      the title of the card
	 * @param cardContent
	 *         the content of the card
	 * @return {@link SpeechletResponse} the speechlet response
	 */
	public SpeechletResponse newTellWithCardResponse(String outputText, boolean isOutputSsml, String cardTitle, String cardContent) {
		OutputSpeech outputSpeech;
		if(isOutputSsml) {
			outputSpeech = new SsmlOutputSpeech();
			((SsmlOutputSpeech) outputSpeech).setSsml(outputText);
		} else {
			outputSpeech = new PlainTextOutputSpeech();
			((PlainTextOutputSpeech) outputSpeech).setText(outputText);
		}
		SimpleCard card = new SimpleCard();
		card.setTitle(cardTitle);
		card.setContent(cardContent);

		return SpeechletResponse.newTellResponse(outputSpeech, card);
	}

	/**
	 * Wrapper for creating the Ask response from the input strings.
	 *
	 * @param outputText
	 * 		the output to be spoken, only plain text (no SSML)
	 * @param repromptText
	 * 		the reprompt for if the user doesn't reply or is misunderstood, only plain text (no SSML)
	 *
	 * @return {@link SpeechletResponse} the speechlet response
	 */
	public SpeechletResponse newAskResponse(String outputText, String repromptText) {
		return newAskResponse(outputText, false, repromptText, false);
	}

	/**
	 * Wrapper for creating the Ask response from the input strings.
	 *
	 * @param outputText
	 * 		the output to be spoken
	 * @param isOutputSsml
	 * 		whether the output text is of type SSML
	 * @param repromptText
	 * 		the reprompt for if the user doesn't reply or is misunderstood.
	 * @param isRepromptSsml
	 * 		whether the reprompt text is of type SSML
	 *
	 * @return {@link SpeechletResponse} the speechlet response
	 */
	public SpeechletResponse newAskResponse(String outputText, boolean isOutputSsml, String repromptText, boolean isRepromptSsml) {
		OutputSpeech outputSpeech, repromptOutputSpeech;
		if (isOutputSsml) {
			outputSpeech = new SsmlOutputSpeech();
			((SsmlOutputSpeech) outputSpeech).setSsml(outputText);
		} else {
			outputSpeech = new PlainTextOutputSpeech();
			((PlainTextOutputSpeech) outputSpeech).setText(outputText);
		}

		if (isRepromptSsml) {
			repromptOutputSpeech = new SsmlOutputSpeech();
			((SsmlOutputSpeech) repromptOutputSpeech).setSsml(repromptText);
		} else {
			repromptOutputSpeech = new PlainTextOutputSpeech();
			((PlainTextOutputSpeech) repromptOutputSpeech).setText(repromptText);
		}
		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(repromptOutputSpeech);
		return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
	}

}
