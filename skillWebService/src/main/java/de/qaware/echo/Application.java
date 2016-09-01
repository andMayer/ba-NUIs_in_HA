package de.qaware.echo;

import com.amazon.speech.speechlet.servlet.SpeechletServlet;
import de.qaware.echo.skill.qivicon.QiviconSpeechlet;
import de.qaware.echo.util.PropertyLoader;
import de.qaware.echo.websocket.QiviconWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;

/**
 * To run the Application including the Servlet for the QIVICON Skill and the QIVICON Websocket Connection
 *
 * @author Andreas Mayer
 */
@SpringBootApplication
@ServletComponentScan
public class Application {

	@Autowired
	private QiviconWebSocketHandler webSocketHandler;

	@Bean
	public ServletRegistrationBean qiviconServlet() {
		SpeechletServlet speechServlet = new SpeechletServlet();
		speechServlet.setSpeechlet(new QiviconSpeechlet(webSocketHandler));

		ServletRegistrationBean servlet = new ServletRegistrationBean(speechServlet, "/qivicon");
		servlet.setName("qivicon");

		return servlet;
	}

	public static void main(String[] args) {
		PropertyLoader.loadProperty("amazon.properties");
		SpringApplication.run(Application.class, args);
	}

}