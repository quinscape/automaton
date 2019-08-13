package de.quinscape.automaton.runtime.config;

import de.quinscape.automaton.runtime.message.IncomingMessageHandler;
import de.quinscape.automaton.runtime.ws.AutomatonWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Collection;

@Configuration
public class WebsocketConfiguration
    implements WebSocketConfigurer
{
    private final static Logger log = LoggerFactory.getLogger(WebsocketConfiguration.class);

    private final ApplicationContext applicationContext;
    private final boolean webSocketEnabled;

    public WebsocketConfiguration(
        ApplicationContext applicationContext,
        @Value("${automaton.enable-websocket:false}") boolean webSocketEnabled
    )
    {
        this.applicationContext = applicationContext;
        this.webSocketEnabled = webSocketEnabled;
    }


    /**
     * We don't want to deal with all the socket.js/stomp stuff for now, so we register our own spring
     * {@link TextWebSocketHandler} implementation.
     *
     * @param registry
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry)
    {
        if (webSocketEnabled)
        {
            final AutomatonWebSocketHandler webSocketHandler = applicationContext.getBean(AutomatonWebSocketHandler.class);
            registry.addHandler(webSocketHandler, "/automaton-ws");
        }
    }
}
