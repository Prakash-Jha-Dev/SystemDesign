package com.hackedagain.truecaller.helpers;

import com.hackedagain.truecaller.entities.TrueCallerProfile;
import com.hackedagain.truecaller.interfaces.IProfile;
import com.hackedagain.truecaller.interfaces.ITrueCallerProfile;

public class TrueCallerProfileAdapter {
    public static ITrueCallerProfile construct(IProfile profile) {
        TrueCallerProfile trueCallerProfile = new TrueCallerProfile(profile.getPhoneNumber(), profile.getName(), null, false);
        return trueCallerProfile;
    }
}
