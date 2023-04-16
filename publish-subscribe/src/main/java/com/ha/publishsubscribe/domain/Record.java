package com.ha.publishsubscribe.domain;

public class Record<K,V> extends Message<K,V> {
    private String topic;
    private int partition;
    private long timestamp;

    public Record(Message<K,V> message, String topic, int partition) {
        super(message.getKey(), message.getValue());
        this.topic = topic;
        this.timestamp =  System.currentTimeMillis();
        this.partition = partition;
    }

    public String getTopic() {
        return topic;
    }

    public int getPartition() {
        return partition;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return this.getTopic() + " "
            + this.getPartition() + " "
            + String.valueOf(this.getTimestamp()) + " "
            + this.getValue();
    }
    
}
