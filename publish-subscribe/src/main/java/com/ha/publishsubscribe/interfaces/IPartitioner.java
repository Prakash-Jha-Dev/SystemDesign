package com.ha.publishsubscribe.interfaces;

public interface IPartitioner {
    public int partition(Object object, int totalPartitions);
}
