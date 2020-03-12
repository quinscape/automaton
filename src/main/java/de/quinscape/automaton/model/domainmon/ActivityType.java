package de.quinscape.automaton.model.domainmon;

/**
 * Enum type for domain monitor activity.
 *
 * Keep in sync with src/message/monitor/useDomainMonitor.js in automaton-js.
 */
public enum ActivityType
{
    /**
     * User/connection stopped interacting with the object.
     *
     * Is not actually stored but just used as a counter signal to {@link #ACTIVE}.
     */
    INACTIVE,

    /**
     * The user/connection is interacting with the entity on the client-side UI level
     */
    ACTIVE,

    /**
     * The user has changed the entity.
     */
    CHANGED,
    /**
     * The user has deleted the entity.
     */
    DELETED
}
