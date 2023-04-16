package com.ha.publishsubscribe.services.singleton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.ha.publishsubscribe.domain.Allocation;
import com.ha.publishsubscribe.domain.TopicDetail;
import com.ha.publishsubscribe.services.DataManager;

@Service
public class TopicManager {
    private Map<String, List<DataManager>> topicsMap;
    private Map<String, TopicDetail> topicDetails;
    private Set<DataManager> dataManagers;
    private Random rand;
    
    public TopicManager() {
        topicsMap = new ConcurrentHashMap<>();
        topicDetails = new ConcurrentHashMap<>();
        dataManagers = new HashSet<>();
        rand = new Random();
    }

    public boolean createTopic(TopicDetail topicDetail) {
        System.out.println("Thread "+ Thread.currentThread().getId()+": is creating new topic:"+topicDetail.getTopicName());
        topicDetails.put(topicDetail.getTopicName(), topicDetail);
        List<DataManager> selectedDataManagers = new ArrayList<>();
        int numPartitionsPerDataManager = topicDetail.getTotalPartitions() / dataManagers.size();
        int extraPartitions = topicDetail.getTotalPartitions() % dataManagers.size();

        Iterator<DataManager> dataManagerIterator = getDataManagerIteratorForAllocation();
        for (int allocatedPartitions = 0; allocatedPartitions < topicDetail.getTotalPartitions(); ) {
            int partitionsForAllocation = numPartitionsPerDataManager + (extraPartitions > 0 ? 1: 0);
            if(extraPartitions > 0) {
                extraPartitions--;
            } 
            List<Allocation> allocations = new ArrayList<>();
            for (int partition = 0; partition < partitionsForAllocation; partition++) {
                allocations.add(new Allocation(topicDetail.getTopicName(), allocatedPartitions + partition));
            }
            if (!dataManagerIterator.hasNext()) {
                dataManagerIterator = dataManagers.iterator();
            }

            DataManager dataManager = dataManagerIterator.next();
            dataManager.assignAllocations(allocations);
            selectedDataManagers.add(dataManager);

            allocatedPartitions += partitionsForAllocation;
        }

        topicsMap.put(topicDetail.getTopicName(), selectedDataManagers);
        return true;
    }

    public TopicDetail getTopicDetail(String topicName) {
        return topicDetails.get(topicName);
    }

    public List<DataManager> getDataManagers(String topicName) {
        return topicsMap.get(topicName);
    }

    public boolean add(DataManager dataManager) {
        dataManagers.add(dataManager);
        return true;
    }

    private Iterator<DataManager> getDataManagerIteratorForAllocation() {
        Iterator<DataManager> dataManagerIterator = dataManagers.iterator();
        int random = rand.nextInt(dataManagers.size());
        while(random-- > 0) {
            dataManagerIterator.next();
        }
        return dataManagerIterator;
    }
}
