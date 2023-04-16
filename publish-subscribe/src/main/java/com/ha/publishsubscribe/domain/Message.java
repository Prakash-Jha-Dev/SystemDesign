package com.ha.publishsubscribe.domain;

public class Message<K,V> extends Object {
    private K key;
    private V value;

    public Message(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}
