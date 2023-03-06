package com.hackedagain.truecaller.interfaces;

public interface IProfileUpdateListener {
    void profileAdded(ITrueCallerProfile profile);
    void profileRemoved(ITrueCallerProfile profile);
    void profileUpdated(ITrueCallerProfile updatedProfile, ITrueCallerProfile incomingProfile);
}
