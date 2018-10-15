package de.quinscape.automaton.runtime.message;


import de.quinscape.automaton.model.message.OutgoingMessage;
import de.quinscape.automaton.runtime.ws.AutomatonClientConnection;
import de.quinscape.automaton.runtime.ws.AutomatonWebSocketHandler;

import java.util.ArrayList;
import java.util.List;

public class PreparedMessages
{
    private final List<PreparedMessage> preparedMessages = new ArrayList<>();

    public PreparedMessages()
    {

    }

    public void add(String connectionId, OutgoingMessage outgoingMessage)
    {
        preparedMessages.add(new PreparedMessage(connectionId, outgoingMessage));
    }


    public List<PreparedMessage> getMessages()
    {
        return preparedMessages;
    }


    public void addAll(List<PreparedMessage> preparedMessages)
    {
        this.preparedMessages.addAll(preparedMessages);
    }

    public void sendAll(AutomatonWebSocketHandler automatonTestWebSocketHandler)
    {
        for (PreparedMessage preparedMessage : preparedMessages)
        {
            final AutomatonClientConnection connection = automatonTestWebSocketHandler.getClientConnection(preparedMessage.getConnectionId());
            if (connection != null)
            {
                connection.send(preparedMessage);
            }
        }
    }
}
