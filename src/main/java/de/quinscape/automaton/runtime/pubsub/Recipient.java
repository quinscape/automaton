package de.quinscape.automaton.runtime.pubsub;

import java.util.List;

/**
 * Internal class encapsulating a receiving topic registration and a list of client-side handler ids to receive
 * the update
 */
final class Recipient
{
    public final TopicRegistration registration;
    public final List<Long> ids;

    Recipient(TopicRegistration registration, List<Long> ids)
    {
        this.registration = registration;
        this.ids = ids;
    }
}
