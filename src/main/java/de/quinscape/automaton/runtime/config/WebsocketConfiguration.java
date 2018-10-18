package de.quinscape.automaton.runtime.config;

import de.quinscape.automaton.runtime.message.GraphQLMessageHandler;
import de.quinscape.automaton.runtime.ws.AutomatonWebSocketHandler;
import graphql.GraphQL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Collections;

@Configuration
public class WebsocketConfiguration
    implements WebSocketConfigurer
{
    private final GraphQL graphQL;

    private final boolean webSocketEnabled;


    public WebsocketConfiguration(
        @Lazy graphql.GraphQL graphQL,
        @Value("${automatontest.enable-websocket:false}") boolean webSocketEnabled
    )
    {
        this.graphQL = graphQL;
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
            final AutomatonWebSocketHandler webSocketHandler = automatonTestWebSocketHandler();
            registry.addHandler(webSocketHandler, "/automaton-ws");
        }
    }

    @Bean
    public AutomatonWebSocketHandler automatonTestWebSocketHandler()
    {
        if (!webSocketEnabled)
        {
            return null;
        }
        return new AutomatonWebSocketHandler(
            Collections.singletonList(
                new GraphQLMessageHandler(
                    graphQL
                )
            )
        );
    }
}
