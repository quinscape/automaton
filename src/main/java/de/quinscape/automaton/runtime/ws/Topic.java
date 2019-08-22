package de.quinscape.automaton.runtime.ws;

import java.util.ArrayList;
import java.util.List;

public class Topic
{
    private final List<AutomatonClientConnection> connections = new ArrayList<>();

    public synchronized void register(AutomatonClientConnection connection)
    {
        connections.add(connection);

    }

    public synchronized void deregister(AutomatonClientConnection connection)
    {
        connections.remove(connection);
    }


    public synchronized List<AutomatonClientConnection> getConnections()
    {
        return new ArrayList<>(connections);
    }
}
