package de.quinscape.automaton.runtime.pubsub;

import de.quinscape.automaton.model.message.IncomingMessage;
import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.JavaFilterTransformer;
import de.quinscape.automaton.runtime.message.IncomingMessageHandler;
import de.quinscape.automaton.runtime.ws.AutomatonClientConnection;
import de.quinscape.domainql.DomainQL;

import java.util.Map;

/**
 * Handles the subscribing and unsubscribing of connections to topics. You need to use this message handler in your
 * {@link de.quinscape.automaton.runtime.ws.AutomatonWebSocketHandler} to use the topic feature.
 * 
 */
public class PubSubMessageHandler
    implements IncomingMessageHandler
{
    private final static String TYPE = "PUBSUB";
    
    private final static String SUBSCRIBE = "SUBSCRIBE";
    private final static String UNSUBSCRIBE = "UNSUBSCRIBE";
    private final static String PUBLISH = "PUBLISH";

    private final DomainQL domainQL;

    private final PubSubService pubSubService;

    private final JavaFilterTransformer javaFilterTransformer;


    public PubSubMessageHandler(
        DomainQL domainQL,
        PubSubService pubSubService,
        JavaFilterTransformer javaFilterTransformer
    )
    {
        this.domainQL = domainQL;
        this.pubSubService = pubSubService;
        this.javaFilterTransformer = javaFilterTransformer;
    }


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

        final String op = (String) payload.get("op");
        final String topic = (String) payload.get("topic");

        switch (op)
        {
            case SUBSCRIBE:
            {
                final Long id = (Long) payload.get("id");
                final Map<String, Object> raw = (Map<String, Object>) payload.get("filter");
                final Map<String, Object> deserialized = JavaFilterTransformer.deserialize(domainQL, raw, false);
                final Filter filter = javaFilterTransformer.transform(deserialized);
                pubSubService.subscribe(connection, topic, filter, id);
                break;
            }
            case UNSUBSCRIBE:
            {
                final Long id = (Long) payload.get("id");
                pubSubService.unsubscribe(connection, topic, id);
                break;
            }
            case PUBLISH:
            {
                final Object message = payload.get("message");
                pubSubService.publish(topic, message);
                break;
            }
            default:
                throw new IllegalStateException("Unhandled pubsub operation: " + op);
        }
    }
}
