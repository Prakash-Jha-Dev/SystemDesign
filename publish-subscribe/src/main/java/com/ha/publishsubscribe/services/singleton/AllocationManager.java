package com.ha.publishsubscribe.services.singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ha.publishsubscribe.domain.Allocation;
import com.ha.publishsubscribe.domain.TopicDetail;
import com.ha.publishsubscribe.interfaces.IConsumer;
import com.ha.publishsubscribe.interfaces.IConsumerGroup;

@Service
public class AllocationManager {

    @Autowired
    private TopicManager topicManager;

    private Map<IConsumerGroup, List<IConsumer>>consumerGroupConsumerList;
    private Map<IConsumerGroup, Map<IConsumer, List<Allocation>>>currentAllocation;

    public AllocationManager() {
        consumerGroupConsumerList = new ConcurrentHashMap<>();
        currentAllocation = new ConcurrentHashMap<>();
    }

    public AllocationManager(TopicManager topicManager) {
        this();
        this.topicManager = topicManager;
    }

    public void reallocate(IConsumerGroup consumerGroup) {
        TopicDetail topicDetail = topicManager.getTopicDetail(consumerGroup.getSubscribedTopicName());
        Iterator<IConsumer> it = consumerGroupConsumerList.get(consumerGroup).iterator();
        Map<IConsumer, List<Allocation>>allocations = new HashMap<>();
        for (int partition=0; partition<topicDetail.getTotalPartitions(); partition++) {
            if (!it.hasNext()) {
                it = consumerGroupConsumerList.get(consumerGroup).iterator();
            }
            IConsumer consumer = it.next();
            Allocation allocation = new Allocation(consumerGroup.getSubscribedTopicName(), partition);
            List<Allocation>currentAllocation = allocations.getOrDefault(consumer, new ArrayList<Allocation>());
            currentAllocation.add(allocation);
            allocations.put(consumer, currentAllocation);
        }

        updateAllocations(allocations, consumerGroupConsumerList.get(consumerGroup));
        currentAllocation.put(consumerGroup, allocations);
        System.out.println("Updated Allocations:: "+ allocations);
        
    }

    public void subscribe(IConsumer consumer) {
        List<IConsumer> consumers = consumerGroupConsumerList.getOrDefault(consumer.getConsumerGroup(), new ArrayList<IConsumer>());
        consumers.add(consumer);
        consumerGroupConsumerList.put(consumer.getConsumerGroup(), consumers);
    }

    private void updateAllocations(Map<IConsumer, List<Allocation>>updatedAllocations, List<IConsumer> consumers) {
        for (IConsumer consumer: consumers) {
            consumer.updateAllocations(updatedAllocations.getOrDefault(consumer, new ArrayList<Allocation>()));
        }
    }
}
