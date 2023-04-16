package com.ha.publishsubscribe.interfaces;

import com.ha.publishsubscribe.domain.Record;

public interface IProducer {
    public <K,V> boolean send(Record<K,V> record);
}
