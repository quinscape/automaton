package de.quinscape.automaton.runtime.pubsub;

import de.quinscape.automaton.runtime.auth.AutomatonAuthentication;
import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.JavaFilterTransformer;
import de.quinscape.automaton.runtime.util.Base32;
import de.quinscape.automaton.runtime.ws.DefaultAutomatonClientConnection;
import de.quinscape.automaton.runtime.ws.DefaultAutomatonWebSocketHandler;
import de.quinscape.domainql.DomainQL;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.svenson.util.JSONPathUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.quinscape.automaton.runtime.scalar.ConditionBuilder.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class PubSubServiceTest
{

    private final static Logger log = LoggerFactory.getLogger(PubSubServiceTest.class);

    private final DomainQL domainQL = DomainQL.newDomainQL(null).build();
    private final static JSONPathUtil pathUtil = new JSONPathUtil(JSONUtil.OBJECT_SUPPORT);

    private PubSubService pubSubSvc = new DefaultPubSubService();

    private JavaFilterTransformer javaFilterTransformer = new JavaFilterTransformer();

    private DefaultAutomatonWebSocketHandler webSocketHandler = new DefaultAutomatonWebSocketHandler(
        Arrays.asList(
            new PubSubMessageHandler(
                domainQL, pubSubSvc,
                javaFilterTransformer
            )
        )
    );


    @Test
    public void testPubSub() throws IOException
    {

        final String cid = Base32.uuid();
        TestWebSocketSession session = new TestWebSocketSession(cid);
        final DefaultAutomatonClientConnection connection = new DefaultAutomatonClientConnection(
            cid,
            AutomatonAuthentication.current()
        );
        webSocketHandler.register(connection);
        webSocketHandler.afterConnectionEstablished(session);

        final String testTopic = "Test-Topic";

        webSocketHandler.handleTextMessage(session, new TextMessage("{\n" +
            "    \"type\": \"PUBSUB\",\n" +
            "    \"connectionId\" : \"" + cid + "\",\n" +
            "    \"messageId\" : \"m1\",\n" +
            "    \"payload\": {\n" +
            "        \"op\": \"SUBSCRIBE\",\n" +
            "        \"topic\": \"" + testTopic + "\",\n" +
            "        \"id\" : 2982\n" +
            "    }\n" +
            "}"));

        final TestTopicListener topicListener = new TestTopicListener();
        pubSubSvc.subscribe(topicListener, testTopic, null,5294L);

        final Topic test = pubSubSvc.getTopics(testTopic);

        final List<Set<TopicRegistration>> registrationsByConnection = test.getRegistrationsByConnection();

        assertThat(registrationsByConnection.size(), is(2));
        assertThat(registrationsByConnection.get(0).size(), is(1));
        assertThat(registrationsByConnection.get(1).size(), is(1));

        TopicRegistration registration = registrationsByConnection.get(0).iterator().next();
        TopicRegistration listenerRegistration = registrationsByConnection.get(1).iterator().next();

        if (listenerRegistration.getConnection() != null)
        {
            final TopicRegistration tmp = registration;
            registration = listenerRegistration;
            listenerRegistration = tmp;
        }

        assertThat(registration.getConnection(), is(connection));
        assertThat(registration.getId(), is(2982L));
        assertThat(registration.getTopicListener(), is(nullValue()));

        assertThat(listenerRegistration.getConnection(), is(nullValue()));
        assertThat(listenerRegistration.getTopicListener(), is(topicListener));
        assertThat(listenerRegistration.getId(), is(5294L));

        pubSubSvc.publish(testTopic, new TestPayload("payload-0", 1122));

        final List<WebSocketMessage> messages = session.getMessages();
        assertThat(messages.size(), is(1));

        final TextMessage webSocketMessage = (TextMessage) messages.get(0);


        final Map result = JSONUtil.DEFAULT_PARSER.parse(Map.class, webSocketMessage.getPayload());

        //log.info(result.toString());

        assertThat(pathUtil.getPropertyPath(result, "type"), is("TOPIC"));
        assertThat(pathUtil.getPropertyPath(result, "payload.ids"), is(Arrays.asList(2982L)));
        assertThat(pathUtil.getPropertyPath(result, "payload.topic"), is(testTopic));
        assertThat(pathUtil.getPropertyPath(result, "payload.payload.name"), is("payload-0"));
        assertThat(pathUtil.getPropertyPath(result, "payload.payload.num"), is(1122L));

        TestPayload testPayload = (TestPayload) ((TopicUpdate) topicListener.getOutgoingMessage().getPayload()).getPayload();
        assertThat(testPayload.getName(), is("payload-0"));
        assertThat(testPayload.getNum(), is(1122));

        pubSubSvc.unsubscribe(connection, testTopic, 2982L);
        pubSubSvc.unsubscribe(topicListener, testTopic, 5294L);

        assertThat(test.getRegistrationsByConnection().get(0).size(), is(0));
        assertThat(test.getRegistrationsByConnection().get(1).size(), is(0));
    }

    @Test
    public void testFilteredPubSub() throws IOException
    {

        final String cid = Base32.uuid();
        TestWebSocketSession session = new TestWebSocketSession(cid);
        final DefaultAutomatonClientConnection connection = new DefaultAutomatonClientConnection(
            cid,
            AutomatonAuthentication.current()
        );
        final String testTopic = "Filtered-Test-Topic";

        webSocketHandler.register(connection);
        webSocketHandler.afterConnectionEstablished(session);
        final Map<String, Object> filter = condition("eq", Arrays.asList(
            field("name"),
            value("String", "aaa")
        ));
        webSocketHandler.handleTextMessage(session, new TextMessage("{\n" +
            "    \"type\": \"PUBSUB\",\n" +
            "    \"connectionId\" : \"" + cid + "\",\n" +
            "    \"messageId\" : \"m1\",\n" +
            "    \"payload\": {\n" +
            "        \"op\": \"SUBSCRIBE\",\n" +
            "        \"topic\": \"" + testTopic + "\",\n" +
            "        \"id\" : 8312,\n" +
            "        \"filter\" : " + JSONUtil.DEFAULT_GENERATOR.forValue(filter) +
            "    }\n" +
            "}"));

        final TestTopicListener topicListener = new TestTopicListener();


        final Filter filter2 = javaFilterTransformer.transform(
            condition("eq", Arrays.asList(
                field("num"),
                value("Int", 3456)
            ))
        );

        pubSubSvc.subscribe(topicListener, testTopic, filter2,6239L);

        final Topic test = pubSubSvc.getTopics(testTopic);

        final List<Set<TopicRegistration>> registrationsByConnection = test.getRegistrationsByConnection();

        assertThat(registrationsByConnection.size(), is(2));
        assertThat(registrationsByConnection.get(0).size(), is(1));
        assertThat(registrationsByConnection.get(1).size(), is(1));


        TopicRegistration registration = registrationsByConnection.get(0).iterator().next();
        TopicRegistration listenerRegistration = registrationsByConnection.get(1).iterator().next();

        if (listenerRegistration.getConnection() != null)
        {
            final TopicRegistration tmp = registration;
            registration = listenerRegistration;
            listenerRegistration = tmp;
        }

        assertThat(registration.getConnection(), is(connection));
        assertThat(registration.getTopicListener(), is(nullValue()));
        assertThat(registration.getId(), is(8312L));

        assertThat(listenerRegistration.getConnection(), is(nullValue()));
        assertThat(listenerRegistration.getTopicListener(), is(topicListener));
        assertThat(listenerRegistration.getId(), is(6239L));

        pubSubSvc.publish(testTopic, new TestPayload("bbb", 3456));
        pubSubSvc.publish(testTopic, new TestPayload("aaa", 2345));

        final List<WebSocketMessage> messages = session.getMessages();
        assertThat(messages.size(), is(1));

        final TextMessage webSocketMessage = (TextMessage) messages.get(0);


        final Map result = JSONUtil.DEFAULT_PARSER.parse(Map.class, webSocketMessage.getPayload());

        //log.info(result.toString());

        assertThat(pathUtil.getPropertyPath(result, "type"), is("TOPIC"));
        assertThat(pathUtil.getPropertyPath(result, "payload.ids"), is(Arrays.asList(8312L)));
        assertThat(pathUtil.getPropertyPath(result, "payload.topic"), is(testTopic));
        assertThat(pathUtil.getPropertyPath(result, "payload.payload.name"), is("aaa"));
        assertThat(pathUtil.getPropertyPath(result, "payload.payload.num"), is(2345L));

        TestPayload testPayload = (TestPayload) ((TopicUpdate) topicListener.getOutgoingMessage().getPayload()).getPayload();
        assertThat(testPayload.getName(), is("bbb"));
        assertThat(testPayload.getNum(), is(3456));


    }
}
