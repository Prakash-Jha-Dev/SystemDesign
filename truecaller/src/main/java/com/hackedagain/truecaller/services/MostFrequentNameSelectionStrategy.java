package com.hackedagain.truecaller.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.hackedagain.truecaller.interfaces.INameSelectionStrategy;

@Service
public class MostFrequentNameSelectionStrategy implements INameSelectionStrategy {

    private Set<String> ignoredNames = ConcurrentHashMap.newKeySet();
    
    public void updateIgnoredNames(Collection<? extends String> filetredNames) {
        ignoredNames.clear();
        ignoredNames.addAll(filetredNames);
    }

    @Override
    public String getName(Set<String> names) {
        Map<String, Integer> freqMap = new HashMap<>();
        for(String name: names) {
            freqMap.put(name, freqMap.getOrDefault(name, 0) + 1);
        }
        String selectedName = "";
        Integer maxFreq = 0;
        for(String name : freqMap.keySet()) {
            if(!ignoredNames.contains(name)) {
                if(maxFreq < freqMap.get(name)) {
                    maxFreq = freqMap.get(name);
                    selectedName = name;
                }
            }
        }
        if(selectedName == "") {
            selectedName = freqMap.keySet().iterator().next();
        }
        return selectedName;
    }
    
}
