package de.quinscape.automaton.runtime.message;


import de.quinscape.automaton.runtime.ws.AutomatonClientConnection;
import de.quinscape.automaton.runtime.ws.AutomatonWebSocketHandler;

public interface ConnectionListener
{
    void onClose(AutomatonWebSocketHandler webSocketHandler, AutomatonClientConnection connection);
    void onOpen(AutomatonWebSocketHandler webSocketHandler, AutomatonClientConnection connection);
}
