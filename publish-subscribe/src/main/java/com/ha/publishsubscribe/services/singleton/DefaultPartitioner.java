package com.ha.publishsubscribe.services.singleton;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.ha.publishsubscribe.interfaces.IPartitioner;

@Service
public class DefaultPartitioner implements IPartitioner {

    @Override
    public int partition(Object key, int totalPartitions) {
        int partition = Math.abs(Objects.hashCode(key)) % totalPartitions;
        return partition;
    }
    
}
