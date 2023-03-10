package com.hackedagain.truecaller.services;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.hackedagain.truecaller.entities.PhoneNumber;
import com.hackedagain.truecaller.helpers.Trie;
import com.hackedagain.truecaller.interfaces.IProfileUpdateListener;
import com.hackedagain.truecaller.interfaces.ITrueCallerProfile;

@Service
public class TrueCallerDirectoryService implements IProfileUpdateListener {
    private Trie<ITrueCallerProfile> phoneNumberProfileTrie = new Trie<>();
    private Trie<ITrueCallerProfile> nameProfileTrie = new Trie<>();

    public Set<ITrueCallerProfile> searchByPhoneNumber(PhoneNumber phoneNumber) {
        return phoneNumberProfileTrie.get(phoneNumber.get());
    }

    public Set<ITrueCallerProfile> searchByName(String name) {
        return nameProfileTrie.get(name);
    }

    @Override
    public void profileAdded(ITrueCallerProfile profile) {
        phoneNumberProfileTrie.add(profile.getPhoneNumber().get(), profile);
        nameProfileTrie.add(profile.getName(), profile);
    }

    @Override
    public void profileRemoved(ITrueCallerProfile profile) {
        phoneNumberProfileTrie.remove(profile.getPhoneNumber().get(), profile);
        nameProfileTrie.remove(profile.getName(), profile);
    }

    @Override
    public void profileUpdated(ITrueCallerProfile updatedProfile, ITrueCallerProfile incomingProfile) {
        phoneNumberProfileTrie.remove(incomingProfile.getPhoneNumber().get(), updatedProfile);
        phoneNumberProfileTrie.add(incomingProfile.getPhoneNumber().get(), updatedProfile);
        nameProfileTrie.add(incomingProfile.getName(), incomingProfile);
    }
    
}
