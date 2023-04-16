package com.ha.publishsubscribe.interfaces;

import com.ha.publishsubscribe.domain.ConsumerGroupAllocation;

public interface IOffsetStore {
    public int getCommittedOffset(ConsumerGroupAllocation consumerGroupAllocation);
    public boolean updateCommittedOffset(ConsumerGroupAllocation consumerGroupAllocation, int offset);
}
