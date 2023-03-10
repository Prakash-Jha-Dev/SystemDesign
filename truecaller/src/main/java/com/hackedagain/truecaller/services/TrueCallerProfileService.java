package com.hackedagain.truecaller.services;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.hackedagain.truecaller.entities.PhoneNumber;
import com.hackedagain.truecaller.helpers.TrueCallerProfileAdapter;
import com.hackedagain.truecaller.interfaces.INameSelectionStrategy;
import com.hackedagain.truecaller.interfaces.IPhoneBookContactService;
import com.hackedagain.truecaller.interfaces.IProfile;
import com.hackedagain.truecaller.interfaces.IProfileUpdateListener;
import com.hackedagain.truecaller.interfaces.ITrueCallerProfile;

public class TrueCallerProfileService {
    private Map<PhoneNumber, ITrueCallerProfile> profiles;
    private List<IProfileUpdateListener> listeners;
    private final INameSelectionStrategy nameSelectionStrategy;

    public TrueCallerProfileService(INameSelectionStrategy selectionStrategy) {
        this.nameSelectionStrategy = selectionStrategy;
        profiles = new ConcurrentHashMap<PhoneNumber, ITrueCallerProfile>();
        listeners = new CopyOnWriteArrayList<IProfileUpdateListener>();
    }

    public List<ITrueCallerProfile> getAllProfiles() {
        return profiles.values().stream().collect(Collectors.toList());
    }

    public void registerProfile(ITrueCallerProfile profile) {
        if (!profiles.containsKey(profile.getPhoneNumber())) {
            listeners.forEach(listener -> listener.profileAdded(profile));

            profiles.put(profile.getPhoneNumber(), profile);
        } else {
            ITrueCallerProfile existingProfile = profiles.get(profile.getPhoneNumber());
            existingProfile.updateRegisteredNames(profile.getRegisteredNames());
            existingProfile.updateName(nameSelectionStrategy.getName(existingProfile.getRegisteredNames()));
            
            listeners.forEach(listener -> listener.profileUpdated(existingProfile, profile));
        }
    }

    public void removeProfile(ITrueCallerProfile profile) {
        listeners.forEach(listener -> listener.profileRemoved(profile));
        profiles.remove(profile.getPhoneNumber());
    }

    public void updateProfile(ITrueCallerProfile profile) {
        ITrueCallerProfile profileToUpdate = profiles.get(profile.getPhoneNumber());
        profileToUpdate.getRegisteredNames().addAll(profile.getRegisteredNames());
        profiles.put(profile.getPhoneNumber(), profileToUpdate);
    }

    public void updateProfileSpamData(PhoneNumber phoneNumber, ITrueCallerProfile profile, boolean markAsSpam) {
        ITrueCallerProfile profileToUpdate = profiles.get(phoneNumber);
        profileToUpdate.updateSpam(profile, markAsSpam);
    }

    public void importProfileFromContacts(IPhoneBookContactService contactService) {
        for (IProfile profile: contactService.getContacts()) {
            this.registerProfile(TrueCallerProfileAdapter.construct(profile));
        }
    }

    public void addListener(IProfileUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(IProfileUpdateListener listener) {
        listeners.remove(listener);
    }

}
