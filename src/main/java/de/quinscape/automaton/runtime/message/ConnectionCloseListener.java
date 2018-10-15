package de.quinscape.automaton.runtime.message;


import de.quinscape.automaton.runtime.ws.AutomatonClientConnection;
import de.quinscape.automaton.runtime.ws.AutomatonWebSocketHandler;

public interface ConnectionCloseListener
{

    void onClose(AutomatonWebSocketHandler webSocketHandler, AutomatonClientConnection clientConnection);
}
