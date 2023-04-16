package com.ha.publishsubscribe.domain;

public class TopicDetail {
    private String topicName;
    private int totalPartitions;

    public TopicDetail(String topicName, int totalPartitions) {
        this.topicName = topicName;
        this.totalPartitions = totalPartitions;
    }

    public String getTopicName() {
        return topicName;
    }

    public int getTotalPartitions() {
        return totalPartitions;
    }
}
