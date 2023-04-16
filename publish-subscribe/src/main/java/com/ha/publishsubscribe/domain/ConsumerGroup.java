package com.ha.publishsubscribe.domain;

import java.util.Objects;

import com.ha.publishsubscribe.interfaces.IConsumerGroup;

public class ConsumerGroup implements IConsumerGroup {

    private String name;
    private String subscribedTopicName;

    public ConsumerGroup(String name, String topic) {
        this.name = name;
        this.subscribedTopicName = topic;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSubscribedTopicName() {
        return subscribedTopicName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj.getClass() != this.getClass()) {
            return false;
        }

        ConsumerGroup oConsumerGroup = (ConsumerGroup) obj;
        return name.equals(oConsumerGroup.getName());
    }    

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
