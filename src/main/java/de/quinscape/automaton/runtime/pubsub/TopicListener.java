package de.quinscape.automaton.runtime.pubsub;

import de.quinscape.automaton.model.message.OutgoingMessage;

public interface TopicListener
{
    void onMessage(OutgoingMessage outgoingMessage);
}
