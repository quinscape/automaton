package de.quinscape.automaton.runtime.domain;

import de.quinscape.automaton.model.domainmon.ActivityType;
import de.quinscape.automaton.model.domainmon.DomainActivity;
import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterEvaluationContext;
import de.quinscape.automaton.runtime.message.ConnectionListener;
import de.quinscape.automaton.runtime.pubsub.PubSubService;
import de.quinscape.automaton.runtime.pubsub.SubscriptionListener;
import de.quinscape.automaton.runtime.pubsub.TopicListener;
import de.quinscape.automaton.model.pubsub.TopicUpdate;
import de.quinscape.automaton.runtime.ws.AutomatonClientConnection;
import de.quinscape.automaton.runtime.ws.AutomatonWebSocketHandler;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.util.RecastUtil;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides live-updates about the domain activity of other users/web socket connections.
 */
public class DomainMonitorService
{
    private final static Logger log = LoggerFactory.getLogger(DomainMonitorService.class);

    /**
     * Domain Monitor pub sub topic
     * <p>
     * Keep in sync with src/message/monitor/useDomainMonitor.js in automaton-js.
     */
    public static final String DOMAIN_MON_TOPIC = "DomainMon";

    private final ConcurrentHashMap<String, ActivityHolder> domainActivities;

    private final PubSubService pubSubService;


    public DomainMonitorService(PubSubService pubSubService)
    {
        log.debug("Create DomainMonitorService: {}", pubSubService);

        this.pubSubService = pubSubService;

        pubSubService.register(
            new TopicSubscriptionListener()
        );

        pubSubService.getWebSocketHandler().register(
            new MonitorConnectionListener()
        );

        domainActivities = new ConcurrentHashMap<>();

        this.pubSubService.subscribe(
            new MonitorTopicListener(),
            DOMAIN_MON_TOPIC,
            null
        );
    }


    /**
     * Sends all currently known matching activity to a user after they subscribed to the monitor topic with or
     * without a filter.
     */
    private class TopicSubscriptionListener
        implements SubscriptionListener
    {
        public void onSubscribe(AutomatonClientConnection connection, String topic, Filter filter, Long id)
        {
            int mismatch = 0;
            for (ActivityHolder holder : domainActivities.values())
            {
                final List<DomainActivity> domainActivities = holder.getDomainActivities();

                for (DomainActivity activity : domainActivities)
                {
                    final FilterEvaluationContext ctx = new FilterEvaluationContext(activity);
                    if (filter == null || filter.evaluate(ctx).equals(Boolean.TRUE))
                    {
                        log.debug("Send to {}/id={}: {}", connection.getConnectionId(), id, activity);

                        connection.send(
                            TopicUpdate.createMessage(
                                DOMAIN_MON_TOPIC,
                                activity,
                                Collections.singletonList(id)
                            )
                        );
                    }
                    else
                    {
                        mismatch++;
                    }
                }
            }

            if (log.isDebugEnabled())
            {
                log.debug("{} filter rules did not match", mismatch);
            }
        }


        @Override
        public void onUnsubscribe(AutomatonClientConnection connection)
        {
        }

    }

    /**
     * Merges newly published domain activity into the server-side storage
     */
    private class MonitorTopicListener
        implements TopicListener
    {

        @Override
        public void onMessage(Object topicUpdate)
        {
            // recast message to typed activity and new timestamp
            // recasting makes it more convenient to deal with the messages and also enforces a schema on the channel
            // messages.
            final DomainActivity domainActivity = RecastUtil.recast(
                DomainActivity.class,
                topicUpdate,
                JSONUtil.OBJECT_SUPPORT
            ).stamp();

            ActivityHolder holder = new ActivityHolder(domainActivity.getDomainType(), domainActivity.getId());
            final ActivityHolder existing = domainActivities.putIfAbsent(holder.getKey(), holder);

            if (existing != null)
            {
                holder = existing;
            }
            holder.addActivity(domainActivity);
        }
    }

    /**
     * Unregisters domain activity for disconnected websocket connections
     */
    private class MonitorConnectionListener
        implements ConnectionListener
    {

        @Override
        public void onClose(
            AutomatonWebSocketHandler webSocketHandler, AutomatonClientConnection connection
        )
        {
            domainActivities.values().forEach(
                holder -> {

                    final List<DomainActivity> formerlyActives = holder.removeConnectionId(
                        connection.getConnectionId()
                    );

                    formerlyActives.forEach(
                        a -> pubSubService.publish(
                            DOMAIN_MON_TOPIC,
                            a.withType(ActivityType.INACTIVE)
                        )
                    );
                }
            );
        }


        @Override
        public void onOpen(
            AutomatonWebSocketHandler webSocketHandler, AutomatonClientConnection connection
        )
        {

        }
    }
}
