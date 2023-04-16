package com.ha.publishsubscribe.services.singleton;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ha.publishsubscribe.domain.Allocation;
import com.ha.publishsubscribe.domain.Record;
import com.ha.publishsubscribe.interfaces.IProducer;
import com.ha.publishsubscribe.services.DataManager;

@Service
public class Producer implements IProducer {

    @Autowired
    private TopicManager topicManager;

    private Map<Allocation, DataManager> dataManagers;

    public Producer() {
        dataManagers = new ConcurrentHashMap<>();
    }

    public Producer(TopicManager topicManager) {
        this();
        this.topicManager = topicManager;
    }

    @Override
    public <K,V> boolean send(Record<K,V> record) {
        Allocation allocation = new Allocation(record.getTopic(), record.getPartition());

        if (!dataManagers.keySet().contains(allocation) ) {
            populateDataManagers(allocation.getTopicName());
        }
        DataManager dataManager = dataManagers.get(allocation);
        if (dataManager == null) {
            // System.out.printf("DataManager is not available for %s and %d", record.getTopic(), record.getPartition() );
            return false;
        }
        System.out.println(record.toString()+" :: produced by Producer@"+this.hashCode()+" in thread:"+Thread.currentThread().getId());
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return dataManager.publishData(this, allocation, (Object)record);
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
    
}
