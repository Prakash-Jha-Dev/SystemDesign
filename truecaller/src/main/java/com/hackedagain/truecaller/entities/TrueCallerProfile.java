package com.hackedagain.truecaller.entities;

import java.util.HashSet;
import java.util.Set;

import com.hackedagain.truecaller.enums.Category;
import com.hackedagain.truecaller.interfaces.ITrueCallerProfile;

public class TrueCallerProfile implements ITrueCallerProfile {

    private PhoneNumber phoneNumber;
    private String preferredName;
    private Set<String> registeredNames;
    private Set<ITrueCallerProfile> usersMarkedProfileAsSpam;
    private Category category;
    private boolean isVerified;
    private final static long MINIMUM_COUNT_TO_BE_CONSIDERED_AS_SPAM = 50;

    public TrueCallerProfile(PhoneNumber phoneNumber, String name, Category category, boolean isVerified) {
        this.phoneNumber = phoneNumber;
        this.preferredName = name;
        this.registeredNames = new HashSet<String>();
        registeredNames.add(name);
        this.usersMarkedProfileAsSpam = new HashSet<ITrueCallerProfile>();
        this.category = category;
        this.isVerified = isVerified;
    }

    @Override
    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public String getName() {
        return preferredName;
    }

    @Override
    public boolean isSpam() {
        return usersMarkedProfileAsSpam.size() >= MINIMUM_COUNT_TO_BE_CONSIDERED_AS_SPAM;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public void updateSpam(ITrueCallerProfile profile, boolean markAsSpam) {
        if (markAsSpam) {
            usersMarkedProfileAsSpam.add(profile);
        } else {
            usersMarkedProfileAsSpam.remove(profile);
        }
    }

    @Override
    public Set<String> getRegisteredNames() {
        return registeredNames;
    }

    @Override
    public void updateRegisteredNames(Set<String> registeredNames) {
        this.registeredNames.addAll(registeredNames);
    }

    @Override
    public void updateName(String name) {
        this.preferredName = name;
    }
    
    @Override
    public boolean isVerified() {
        return isVerified;
    }

    @Override
    public int hashCode() {
        return this.getPhoneNumber().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !obj.getClass().equals(this.getClass())) {
            return false;
        }
        PhoneNumber otPhoneNumber = ((TrueCallerProfile)obj).getPhoneNumber();
        return phoneNumber.equals(otPhoneNumber);
    }
}
