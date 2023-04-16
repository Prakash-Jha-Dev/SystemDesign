package com.ha.publishsubscribe.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import com.ha.publishsubscribe.domain.Allocation;
import com.ha.publishsubscribe.domain.Record;
import com.ha.publishsubscribe.interfaces.IConsumer;
import com.ha.publishsubscribe.interfaces.IConsumerGroup;
import com.ha.publishsubscribe.services.singleton.AllocationManager;
import com.ha.publishsubscribe.services.singleton.TopicManager;

public class Consumer implements IConsumer {

    private String consumerId;
    private IConsumerGroup consumerGroup;
    private Set<Allocation> allocations;
    private Map<Allocation, DataManager> dataManagers;
    private Map<Allocation, Integer> localOffsetCache;
    private TopicManager topicManager;
    private AllocationManager allocationManager;
    private List<Record> consumedMessages; // Stores messages consumed by it. It is useful for debugging purpose, not intended for production.

    public Consumer(String consumerId, IConsumerGroup consumerGroup, TopicManager topicManager, AllocationManager allocationManager) {
        this.consumerId = consumerId;
        this.consumerGroup = consumerGroup;
        this.topicManager = topicManager;
        this.allocationManager = allocationManager;
        dataManagers = new ConcurrentHashMap<>();
        localOffsetCache = new ConcurrentHashMap<>();
        allocations = new ConcurrentSkipListSet<>();
        this.populateDataManagers(consumerGroup.getSubscribedTopicName());
        allocationManager.subscribe(this);
        // Only for debugging purpose
        consumedMessages = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K,V> boolean consume() {
        for (Allocation allocation: allocations) {
            int lastOffset = localOffsetCache.getOrDefault(allocation, -1);
            int currentOffset = lastOffset + 1;
            Object object = dataManagers.get(allocation).consumeData(this, allocation, currentOffset);
            if (object != null) {
                Record<K,V> record = (Record<K,V>) object;
                consumedMessages.add(record);
                dataManagers.get(allocation).commitOffset(consumerGroup, allocation, currentOffset);
                localOffsetCache.put(allocation, currentOffset);
                System.out.println(record.toString()+" :: consumed by "+consumerId+" in thread:"+Thread.currentThread().getId());
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public boolean commitOffset(Allocation allocation) {
        int lastOffset = localOffsetCache.get(allocation);
        return dataManagers.get(allocation).commitOffset(consumerGroup, allocation, lastOffset);
    }

    @Override
    public IConsumerGroup getConsumerGroup() {
        return consumerGroup;
    }

    @Override
    public String getConsumerId() {
        return consumerId;
    }

    @Override
    public void updateAllocations(List<Allocation> allocations) {
        // Use of List instead of string as this information would come from Network in real-world usage.
        // Network transfer contains serialized data and list is of similar kind
        // It is possible to exchange serialized set over network but that'd require special insert and add method implementation, it is not in scope
        this.allocations.clear();
        this.allocations.addAll(allocations);
    }

    public void requestReallocation() {
        allocationManager.reallocate(consumerGroup);
    } 
    
    private void populateDataManagers(String topicName) {
        List<DataManager> incomingDataManagerDetails = topicManager.getDataManagers(topicName);
        for (DataManager dataManager: incomingDataManagerDetails) {
            Set<Allocation> allocations = dataManager.geAllocations();
            for (Allocation allocation: allocations) {
                dataManagers.put(allocation, dataManager);
            }
        }
    }

    @Override
    public String toString() {
        return consumerId;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj.getClass() != this.getClass()) {
            return false;
        }

        Consumer oConsumer = (Consumer) obj;
        return consumerId.equals(oConsumer.getConsumerId());
    }    

    @Override
    public int hashCode() {
        return Objects.hashCode(consumerId);
    }
}
