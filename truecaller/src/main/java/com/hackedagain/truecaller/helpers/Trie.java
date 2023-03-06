package com.hackedagain.truecaller.helpers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Trie<T> {
    private final static int APPROX_MAX_RESULT_SIZE = 100;
    private TrieNode<T> root;

    public Trie() {
        root = new TrieNode<T>();
    }

    public synchronized void add(String key, T value) {
        TrieNode<T> curr = root;
        for(int i=0; i<key.length(); i++) {
            char currentCharacter = key.charAt(i);
            if(!curr.next.containsKey(currentCharacter)) {
                curr.next.put(currentCharacter, new TrieNode<T>());
            }
            curr.count.incrementAndGet();
            curr = curr.next.get(currentCharacter);
        }
        curr.data.add(value);
        curr.isLeaf.set(true);
    }

    public synchronized void remove(String key, T value) {
        if (!exists(key)) return;

        TrieNode<T> curr = root;
        TrieNode<T> prev = null;
        for(int i=0; i<key.length(); i++) {
            curr.count.decrementAndGet();
            char currentCharacter = key.charAt(i);
            prev = curr;
            curr = curr.next.get(currentCharacter);
            if(curr.count.get() == 1 && prev != null) prev.next.remove(currentCharacter);
            if(prev != root && prev.count.get() == 0) prev = null;
        }
        curr = null;
    }

    public Set<T> get(String key) {
        TrieNode<T> curr = root;
        for(int i=0; i<key.length(); i++) {
            char currentCharacter = key.charAt(i);
            if(!curr.next.containsKey(currentCharacter)) {
                return new HashSet<T>();
            }
            curr = curr.next.get(currentCharacter);
        }

        HashSet<T> profiles = new HashSet<>();
        traverse(profiles, curr, APPROX_MAX_RESULT_SIZE);
        return profiles;
    }

    private void traverse(Set<T> profiles, TrieNode<T> node, int lim) {
        if(node.isLeaf.get()) {
            if(profiles.size() < lim) {
                profiles.addAll(node.data);
            }
            return;
        }
        for(Character c: node.next.keySet()) {
            if(profiles.size() < lim) {
                traverse(profiles, node.next.get(c), lim);
            }
        }
    }

    private boolean exists(String key) {
        TrieNode<T> curr = root;
        for(int i=0; i<key.length(); i++) {
            char currentCharacter = key.charAt(i);
            if(!curr.next.containsKey(currentCharacter)) {
                return false;
            }
            curr = curr.next.get(currentCharacter);
        }
        return curr.isLeaf.get();
    }

}

class TrieNode<T> {
    AtomicInteger count = new AtomicInteger(0);
    AtomicBoolean isLeaf = new AtomicBoolean(false);
    Set<T> data = ConcurrentHashMap.newKeySet();
    Map<Character, TrieNode<T>> next = new ConcurrentHashMap<>();
}
