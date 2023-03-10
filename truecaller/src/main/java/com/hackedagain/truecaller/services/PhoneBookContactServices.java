package com.hackedagain.truecaller.services;

import org.springframework.stereotype.Service;

import com.hackedagain.truecaller.interfaces.IPhoneBookContactService;
import com.hackedagain.truecaller.interfaces.IProfile;

@Service
public class PhoneBookContactServices implements IPhoneBookContactService {
    private IProfile [] contacts;

    @Override
    public IProfile[] getContacts() {
        return contacts;
    }

    public void addContacts(IProfile[] profiles) {
        contacts = profiles;
    }
}
