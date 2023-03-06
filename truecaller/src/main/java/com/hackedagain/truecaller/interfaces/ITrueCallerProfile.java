package com.hackedagain.truecaller.interfaces;

import java.util.Set;

import com.hackedagain.truecaller.entities.PhoneNumber;
import com.hackedagain.truecaller.enums.Category;

public interface ITrueCallerProfile extends IProfile {
    PhoneNumber getPhoneNumber();
    String getName();
    boolean isSpam();
    Category getCategory();
    Set<String> getRegisteredNames();
    boolean isVerified();

    void updateSpam(ITrueCallerProfile profile, boolean markAsSpam);
    void updateRegisteredNames(Set<String> registeredNames);
    void updateName(String name);
}