package de.quinscape.automaton.runtime.pubsub;

import de.quinscape.automaton.runtime.ws.AutomatonClientConnection;

import java.util.List;

/**
 * Internal class encapsulating a receiving topic registration and a list of client-side handler ids to receive
 * the update
 */
class Recipient
{
    public final TopicRegistration registration;
    public final List<Long> ids;

    Recipient(TopicRegistration registration, List<Long> ids)
    {
        this.registration = registration;
        this.ids = ids;
    }


}
