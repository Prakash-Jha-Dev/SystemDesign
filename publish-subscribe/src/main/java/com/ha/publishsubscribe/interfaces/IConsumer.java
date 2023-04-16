package com.ha.publishsubscribe.interfaces;

import java.util.List;

import com.ha.publishsubscribe.domain.Allocation;

public interface IConsumer {
    public <K,V> boolean consume();
    public boolean commitOffset(Allocation allocation);
    public IConsumerGroup getConsumerGroup();
    public void updateAllocations(List<Allocation> allocations);
    public String getConsumerId();
}
