package de.quinscape.automaton.runtime.ws;

import de.quinscape.domainql.param.ParameterProvider;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.util.StringUtils;

public final class AutomatonClientConnectionProvider
    implements ParameterProvider
{

    private final AutomatonWebSocketHandler AutomatonTestWebSocketHandler;


    public AutomatonClientConnectionProvider(AutomatonWebSocketHandler AutomatonTestWebSocketHandler)
    {

        this.AutomatonTestWebSocketHandler = AutomatonTestWebSocketHandler;
    }


    @Override
    public Object provide(DataFetchingEnvironment environment)
    {
        final String connectionId = environment.getContext();
        if (!StringUtils.hasText(connectionId))
        {
            throw new IllegalStateException("No connection id found in data fetching environment context.'");
        }

        final AutomatonClientConnection clientConnection = AutomatonTestWebSocketHandler.getConnection(connectionId);
        if (clientConnection == null)
        {
            throw new IllegalStateException("No client connection for id '" + connectionId + "' from data fetching environment context.");
        }

        return clientConnection;
    }
}
