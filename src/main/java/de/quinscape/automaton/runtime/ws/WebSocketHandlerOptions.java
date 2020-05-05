package de.quinscape.automaton.runtime.ws;

import java.util.concurrent.TimeUnit;

public final class WebSocketHandlerOptions
{

    private final long preparedLifetime;

    /**
     * Default options
     */
    public final static WebSocketHandlerOptions DEFAULT = newOptions().build();


    WebSocketHandlerOptions(long preparedLifetime)
    {
        this.preparedLifetime = preparedLifetime;
    }


    public long getPreparedLifetime()
    {
        return preparedLifetime;
    }


    /**
     * Creates a new options object.
     *
     * @return
     */
    public static WebSocketHandlerOptionsBuilder newOptions()
    {
        return new WebSocketHandlerOptionsBuilder();
    }
}
