package com.ha.publishsubscribe.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.ha.publishsubscribe.domain.Allocation;
import com.ha.publishsubscribe.domain.ConsumerGroupAllocation;
import com.ha.publishsubscribe.domain.Record;
import com.ha.publishsubscribe.interfaces.IConsumer;
import com.ha.publishsubscribe.interfaces.IConsumerGroup;
import com.ha.publishsubscribe.interfaces.IOffsetStore;
import com.ha.publishsubscribe.interfaces.IProducer;
import com.ha.publishsubscribe.services.singleton.TopicManager;

public class DataManager {

    private TopicManager topicManager;
    private IOffsetStore offsetStore;
    
    private Map<Allocation, ArrayList<Object>> data;

    public DataManager(TopicManager topicManager, IOffsetStore offsetStore) {
        this.data = new ConcurrentHashMap<>();
        this.topicManager = topicManager;
        this.offsetStore = offsetStore;
        this.register();
    }

    public synchronized boolean publishData(IProducer producer, Allocation allocation, Object object) {
        ArrayList<Object>currData = this.data.get(allocation);
        currData.add(object);
        data.put(allocation, currData);
        
        System.out.println(((Record)object).toString()+" :: stored by DataManager@"+this.hashCode()+" in thread:"+Thread.currentThread().getId());
        return true;
    }

    public int getCommittedOffset(IConsumerGroup consumerGroup, Allocation allocation) {
        ConsumerGroupAllocation consumerGroupAllocation = new ConsumerGroupAllocation(consumerGroup, allocation);
        return offsetStore.getCommittedOffset(consumerGroupAllocation);
    }

    public boolean commitOffset(IConsumerGroup consumerGroup, Allocation allocation, int offset) {
        ConsumerGroupAllocation consumerGroupAllocation = new ConsumerGroupAllocation(consumerGroup, allocation);
        return offsetStore.updateCommittedOffset(consumerGroupAllocation, offset);
    }

    public Object consumeData(IConsumer consumer, Allocation allocation, int offset) {
        Object object = null;
        if(offset < data.get(allocation).size()) {
            object = data.get(allocation).get(offset);
        }
        return object;
    }
    
    public Set<Allocation> geAllocations() {
        return data.keySet();
    }

    public boolean assignAllocations(List<Allocation> newAllocations) {
        for (Allocation allocation: newAllocations) {
            data.put(allocation, new ArrayList<Object>());
        }
        return true;
    }

    private void register() {
        topicManager.add(this);
    }
}
