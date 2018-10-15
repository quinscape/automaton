package de.quinscape.automaton.runtime.message;

import de.quinscape.automaton.model.message.OutgoingMessage;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.svenson.JSONable;

/**
 * Special outgoing message that does an early JSONification of another message to be able to generate
 * the JSON dump of an object within a block synchronized to this object, but not doing to actual message
 * communication within that block.
 */
public final class PreparedMessage
    extends OutgoingMessage
    implements JSONable
{

    private final String connectionId;

    private final String json;

    private final OutgoingMessage outgoingMessage;


    public PreparedMessage(String connectionId, OutgoingMessage outgoingMessage)
    {
        super(outgoingMessage.getType(), outgoingMessage.getPayload(), outgoingMessage.getError());
        this.connectionId = connectionId;
        this.json = JSONUtil.DEFAULT_GENERATOR.forValue(outgoingMessage);
        this.outgoingMessage = outgoingMessage;
    }


    public String getConnectionId()
    {
        return connectionId;
    }


    public OutgoingMessage getOutgoingMessage()
    {
        return outgoingMessage;
    }


    public static OutgoingMessage unwrap(OutgoingMessage outgoingMessage)
    {
        if (outgoingMessage instanceof PreparedMessage)
        {
            return ((PreparedMessage) outgoingMessage).getOutgoingMessage();
        }

        return outgoingMessage;
    }

    @Override
    public String toJSON()
    {
        return json;
    }
}
