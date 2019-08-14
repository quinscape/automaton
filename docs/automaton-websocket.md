# Automaton WebSocket Integration

Automaton integrates Spring Websockets without Socket.js / STOMP as simple in-application websocket solution.

In general the websocket system should be used for small amounts of dynamic data. It is a good practice to keep 
bigger data transfers to normal GraphQL over HTTP (ziplet compression).

The message system is taken from one of the exploratory prototypes and forms an addition or partial 
replacement (see caveat above) for GraphQL.


## Configuring Automaton Websockets

First of all you need to set the System property "automaton.enable-websocket" to true.

Then you need to define a Spring of type [AutomatonWebSocketHandler](https://github.com/quinscape/automaton/blob/master/src/main/java/de/quinscape/automaton/runtime/ws/AutomatonWebSocketHandler.java) with
the [IncomingMessageHandler](https://github.com/quinscape/automaton/blob/master/src/main/java/de/quinscape/automaton/runtime/message/IncomingMessageHandler.java) implementations you need.

There is a built-in incoming message handler called [GraphQLMessageHandler](https://github.com/quinscape/automaton/blob/master/src/main/java/de/quinscape/automaton/runtime/message/GraphQLMessageHandler.java) that uses
the request/response pairing to do GraphQL-over-Websocket. (Again, use this for small data-sets).

```java 
@Bean
public AutomatonWebSocketHandler automatonWebSocketHandler(
    DomainQL domainQL)
{

    return new AutomatonWebSocketHandler(
        Collections.singleton(
            new GraphQLMessageHandler(domainQL)
        )
    );
}
```   

The automaton configuration will detect the property and find the spring bean and provide the necessary `connectionId`
parameter within the initial data block.

## IncomingMessage

Messages come in two flavors, incoming and outgoing messages. (de.quinscape.automaton.model.message.IncomingMessage and de.quinscape.automaton.model.message.OutgoingMessage)

```java 
public final class IncomingMessage
{
    private final String type;
    private final Object payload;
    private final String connectionId;
    private final String messageId;

    // getter & setter etc pp ... 
}      
```

The `type` field defines what kind of message it is which client and
server handling their own type namespace.

The `payload` property contains the actual message payload which in the
case of OutgoingMessage can be any svenson compatible POJO. In the case
of IncomingMessage it will be Map/List graph of the incoming JSON data.

The `connectionId` field contains the connection id of the client that
sent the message.

The `messageId` is used to reference the original message if the server
replies to an client message.

The application needs to register one incoming message handler for each 
distinct `type` value the JavaScript code sends.


## OutgoingMessage

The OutgoingMessage is even simpler then IncomingMessage 

```java 

public final class OutgoingMessage
{
    private final Object payload;

    private final String type;

    // getter & setter etc pp ... 
}
```

The `payload` property contains the actual message POJO payload. 

## Request / Response pairing

Automaton Websockets support request / response pairing of Websocket requests. The client uses the
`Hub.request` method and the server responds with a Response 

## JavaScript API

The JavaScript API of the Automaton Websocket integration is provided by the
[Hub module of NPM "@quinscape/automaton-js"](https://github.com/quinscape/automaton-js/blob/master/docs/Hub.md).

## AutomatonWebSocketHandler

AutomatonWebSocketHandler is the central service for Websocket handling on the server-side

```java 
/**
 * Provides methods to handle websocket messages on the server side.
 * 
 */
public interface AutomatonWebSocketHandler
    extends WebSocketHandler
{
    /**
     * Returns the client connection with the given connection id.
     *
     * @param connectionId      connection id
     *
     * @return  client connection
     */
    AutomatonClientConnection getClientConnection(String connectionId);

    /**
     * Preregisters the given connection id to be associated with the given authentication.
     *
     * When the client opens the websocket connection it will pass back the id so we can
     * associate the given out with websocket messages.
     *
     * This method is used internally when websocket support is enabled.
     *
     * @see de.quinscape.automaton.runtime.provider.AutomatonJsViewProvider#provideCommonData(JsViewContext) 
     */
    void register(AutomatonClientConnection AutomatonClientConnection);


    /**
     * Registers the given ConnectionListener to be notified of every opened and closed connection.
     *
     * @param listener      listener
     */
    void register(ConnectionListener listener);


    /**
     * Returns the currently connection client connections
     *
     * @return read-only collection of all connections
     */
    Collection<AutomatonClientConnection> getConnections();

    /**
     * Sends the given message to all active websocket connections.
     *
      * @param message  message sent to all connections
     *                 
     * @see AutomatonClientConnection#send(OutgoingMessage) 
     * @see AutomatonClientConnection#respond(String, Object)  
     * @see AutomatonClientConnection#respond(String, Object, String)   
     */
    void broadcast(OutgoingMessage message);

    /**
     * Sends the given message to all active websocket connections but one
     *
     * @param message                   message sent to all connections
     * @param excludedConnectionId      connection id to exclude from the broadcast
     *
     * @see AutomatonClientConnection#send(OutgoingMessage)
     * @see AutomatonClientConnection#respond(String, Object)
     * @see AutomatonClientConnection#respond(String, Object, String)
     */
    void broadcast(OutgoingMessage message, String excludedConnectionId);
}
```


## AutomatonClientConnection

```java 
public interface AutomatonClientConnection
{
    String getConnectionId();

    AutomatonAuthentication getAuth();

    void send(OutgoingMessage message);

    void respond(String messageId, Object payload, String error);

    void respond(String messageId, Object result);

    (* ... */
}
```

The AutomatonClientConnection interface then allows sending messages and responding to incoming messages
that are part of a request/response pairing.  
