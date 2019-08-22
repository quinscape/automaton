package de.quinscape.automaton.runtime.message;

import de.quinscape.automaton.model.message.IncomingMessage;
import de.quinscape.automaton.runtime.ws.AutomatonClientConnection;
import de.quinscape.automaton.runtime.ws.AutomatonWebSocketHandler;

import java.util.Map;

/**
 * Handles the registration and deregistration of connections to topics. You need to use this message handler in your
 * {@link de.quinscape.automaton.runtime.ws.AutomatonWebSocketHandler} to use the topic feature.
 * 
 */
public class TopicMessageHandler
    implements IncomingMessageHandler, AutomatonWebSocketHandlerAware
{
    private final static String TYPE = "TOPIC";

    private AutomatonWebSocketHandler webSocketHandler;


    @Override
    public String getMessageType()
    {
        return TYPE;
    }


    @Override
    public void handle(
        IncomingMessage msg, AutomatonClientConnection connection
    )
    {
        final Map<String,Object> payload = (Map<String, Object>) msg.getPayload();



        final TopicOperation op = TopicOperation.valueOf((String) payload.get("op"));

        if (op == TopicOperation.REGISTER)
        {

        }
        else if (op == TopicOperation.DEREGISTER)
        {
            
        }
        else
        {
            throw new IllegalStateException("Unhandled operation: " + op);
        }

        final String topic = (String) payload.get("topic");

    }


    @Override
    public void setAutomatonWebSocketHandler(AutomatonWebSocketHandler webSocketHandler)
    {
        this.webSocketHandler = webSocketHandler;
    }
}
