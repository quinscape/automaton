package de.quinscape.automaton.runtime.ws;

import java.util.concurrent.TimeUnit;

public final class WebSocketHandlerOptionsBuilder
{
    private static final long DEFAULT_PREPARED_LIFETIME = TimeUnit.MINUTES.toMillis(20);
    private final static long DEFAULT_INTERVAL = TimeUnit.MINUTES.toMillis(10);

    private long cleanupInterval = DEFAULT_INTERVAL;

    private long preparedLifetime = DEFAULT_PREPARED_LIFETIME;


    public long getCleanupInterval()
    {
        return cleanupInterval;
    }


    /**
     * Sets the amount of milliseconds the cleanup thread sleeps before checking for stale prepared connections again. (Default 10 minutes)
     *
     * @param cleanupInterval   cleanup interval in ms.
     *
     * @return the builder itself
     */
    public WebSocketHandlerOptionsBuilder withCleanupInterval(long cleanupInterval)
    {
        this.cleanupInterval = cleanupInterval;
        return this;
    }

    public long getPreparedConnectionLifetime()
    {
        return preparedLifetime;
    }


    /**
     * Sets the amount of milliseconds a connection can stay in the prepared state before being removed (Default 30 minutes)
     *
     * @param preparedLifetime      lifetime in ms
     *
     * @return the builder itself
     */
    public WebSocketHandlerOptionsBuilder withPreparedConnectionLifetime(long preparedLifetime)
    {
        this.preparedLifetime = preparedLifetime;
        return this;
    }


    public WebSocketHandlerOptions build()
    {
        return new WebSocketHandlerOptions(
            this.cleanupInterval,
            this.preparedLifetime
        );
    }

}
