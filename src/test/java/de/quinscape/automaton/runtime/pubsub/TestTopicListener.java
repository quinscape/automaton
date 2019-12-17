package de.quinscape.automaton.runtime.pubsub;

public class TestTopicListener
    implements TopicListener
{
    private Object topicUpdate;

    public Object getTopicUpdate()
    {
        return topicUpdate;
    }


    @Override
    public void onMessage(Object topicUpdate)
    {
        this.topicUpdate = topicUpdate;
    }
}
