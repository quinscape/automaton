package de.quinscape.automaton.runtime.pubsub;

import de.quinscape.automaton.model.message.OutgoingMessage;

public class TestTopicListener
    implements TopicListener
{
    private OutgoingMessage outgoingMessage;


    @Override
    public void onMessage(OutgoingMessage outgoingMessage)
    {

        this.outgoingMessage = outgoingMessage;
    }


    public OutgoingMessage getOutgoingMessage()
    {
        return outgoingMessage;
    }
}
