package com.ha.publishsubscribe.domain;

import java.util.Objects;

import com.ha.publishsubscribe.interfaces.IConsumerGroup;

public class ConsumerGroupAllocation {
    private IConsumerGroup consumerGroup;
    private Allocation allocation;

    public ConsumerGroupAllocation(IConsumerGroup consumerGroup, Allocation allocation) {
        this.consumerGroup = consumerGroup;
        this.allocation = allocation;
    }

    public String getConsumerGroupName() {
        return this.consumerGroup.getName();
    }

    public Allocation getAllocation() {
        return this.allocation;
    }

    public String getTopicName() {
        return allocation.getTopicName();
    }

    public int getPartition() {
        return allocation.getPartition();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if(obj.getClass() != this.getClass()) {
            return false;
        } else {
            ConsumerGroupAllocation cag = (ConsumerGroupAllocation) obj;
            return this.allocation.equals(cag.allocation) && 
                this.consumerGroup.equals(cag.consumerGroup);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(allocation, consumerGroup);
    }
}
