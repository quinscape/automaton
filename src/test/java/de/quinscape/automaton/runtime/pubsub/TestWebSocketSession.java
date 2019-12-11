package de.quinscape.automaton.runtime.pubsub;

import de.quinscape.automaton.runtime.domain.monitor.DomainTypeUsage;
import de.quinscape.automaton.runtime.util.Base32;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestWebSocketSession
    implements WebSocketSession
{

    private final String cid;

    private final URI uri;

    private List<WebSocketMessage> messages = new ArrayList<>();

    private Map<String, Object> attrs = new HashMap<>();


    public TestWebSocketSession(String cid)
    {

        this.cid = cid;
        this.uri = URI.create("ws://localhost:8080/automaton-ws?cid=" + cid);
    }


    @Override
    public String getId()
    {
        return null;
    }


    @Override
    public URI getUri()
    {
        return uri;
    }


    @Override
    public HttpHeaders getHandshakeHeaders()
    {
        return null;
    }


    @Override
    public Map<String, Object> getAttributes()
    {
        return attrs;
    }


    @Override
    public Principal getPrincipal()
    {
        return null;
    }


    @Override
    public InetSocketAddress getLocalAddress()
    {
        return null;
    }


    @Override
    public InetSocketAddress getRemoteAddress()
    {
        return null;
    }


    @Override
    public String getAcceptedProtocol()
    {
        return null;
    }


    @Override
    public void setTextMessageSizeLimit(int messageSizeLimit)
    {

    }


    @Override
    public int getTextMessageSizeLimit()
    {
        return 0;
    }


    @Override
    public void setBinaryMessageSizeLimit(int messageSizeLimit)
    {

    }


    @Override
    public int getBinaryMessageSizeLimit()
    {
        return 0;
    }


    @Override
    public List<WebSocketExtension> getExtensions()
    {
        return null;
    }


    @Override
    public void sendMessage(WebSocketMessage<?> message) throws IOException
    {
        messages.add(message);
    }

    public void clearMessages()
    {
        messages.clear();
    }


    public List<WebSocketMessage> getMessages()
    {
        return messages;
    }


    @Override
    public boolean isOpen()
    {
        return false;
    }


    @Override
    public void close() throws IOException
    {

    }


    @Override
    public void close(CloseStatus status) throws IOException
    {

    }
}
