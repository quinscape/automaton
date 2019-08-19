package de.quinscape.automaton.runtime.ws;

import java.util.concurrent.TimeUnit;

public final class WebSocketHandlerOptions
{
    private final static long DEFAULT_INTERVAL = TimeUnit.MINUTES.toMillis(10);
    private static final long DEFAULT_PREPARED_LIFETIME = TimeUnit.MINUTES.toMillis(30);

    private final long cleanupInterval;

    private final long preparedLifetime;

    /**
     * Default options
     */
    public final static WebSocketHandlerOptions DEFAULT = newOptions().build();
    
    WebSocketHandlerOptions(long cleanupInterval, long preparedLifetime)
    {
        this.cleanupInterval = cleanupInterval;
        this.preparedLifetime = preparedLifetime;
    }


    public long getCleanupInterval()
    {
        return cleanupInterval;
    }


    public long getPreparedLifetime()
    {
        return preparedLifetime;
    }


    /**
     * Creates a new options object.
     *
     *
     * @return
     */
    public static WebSocketHandlerOptionsBuilder newOptions()
    {
        return new WebSocketHandlerOptionsBuilder();
    }
}
