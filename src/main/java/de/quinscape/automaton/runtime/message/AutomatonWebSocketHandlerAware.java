package de.quinscape.automaton.runtime.message;

import de.quinscape.automaton.runtime.ws.AutomatonWebSocketHandler;

/**
 * Implemented by incoming message handlers or connection listeners that need access to the application web socket handler.
 */
public interface AutomatonWebSocketHandlerAware
{
    /**
     * Provides the web socket handler.
     *
     * @param webSocketHandler  web socket handler
     */
    void setAutomatonWebSocketHandler(AutomatonWebSocketHandler webSocketHandler);
}
