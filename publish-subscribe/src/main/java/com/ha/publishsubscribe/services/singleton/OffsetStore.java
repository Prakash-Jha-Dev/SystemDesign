package com.ha.publishsubscribe.services.singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.ha.publishsubscribe.domain.ConsumerGroupAllocation;
import com.ha.publishsubscribe.interfaces.IOffsetStore;

@Service
public class OffsetStore implements IOffsetStore {

    private Map<ConsumerGroupAllocation, Integer> store;

    public OffsetStore() {
        this.store = new ConcurrentHashMap<>();
    }

    @Override
    public int getCommittedOffset(ConsumerGroupAllocation consumerGroupAllocation) {
        return store.get(consumerGroupAllocation);
    }

    @Override
    public boolean updateCommittedOffset(ConsumerGroupAllocation consumerGroupAllocation, int offset) {
        store.put(consumerGroupAllocation, Integer.valueOf(offset));
        return true;
    }
    
}
