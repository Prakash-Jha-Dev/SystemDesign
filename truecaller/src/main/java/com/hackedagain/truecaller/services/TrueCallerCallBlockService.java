package com.hackedagain.truecaller.services;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.hackedagain.truecaller.entities.PhoneNumber;

@Service
public class TrueCallerCallBlockService {
    private Set<PhoneNumber> blockedPhoneNumbers = ConcurrentHashMap.newKeySet();

    public void addNumberToBlockList(PhoneNumber phoneNumber) {
        blockedPhoneNumbers.add(phoneNumber);
    }

    public void removeNumberFromBlockList(PhoneNumber phoneNumber) {
        blockedPhoneNumbers.remove(phoneNumber);
    }

    public boolean isPhoneNumberBlocked(PhoneNumber phoneNumber) {
        return blockedPhoneNumbers.contains(phoneNumber);
    }
}
