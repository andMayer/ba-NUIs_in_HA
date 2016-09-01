package de.qaware.echo.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Configuration for the QiviconWebsocketHandler.
 *
 * @author Andreas Mayer
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(getQiviconHandler(), "/qiviconWebsocket").setAllowedOrigins("*");
	}

	@Bean
	public QiviconWebSocketHandler getQiviconHandler() {
		return new QiviconWebSocketHandler(objectMapper);
	}

}
