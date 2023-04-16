package com.ha.publishsubscribe.services;

import com.ha.publishsubscribe.domain.Message;
import com.ha.publishsubscribe.domain.TopicDetail;
import com.ha.publishsubscribe.interfaces.IPartitioner;
import com.ha.publishsubscribe.domain.Record;

public class RecordProducer {
    private IPartitioner partitioner;
    private TopicDetail topicDetail;

    public RecordProducer(TopicDetail topicDetail, IPartitioner partitioner) {
        this.topicDetail = topicDetail;
        this.partitioner = partitioner;
    }

    public <K,V> Record<K,V> createRecord(Message<K,V> message) {
        int partition = partitioner.partition(message.getKey(), topicDetail.getTotalPartitions());
        Record<K,V> record =  new Record<K,V>(message, topicDetail.getTopicName(), partition);
        return record;
    }
}
