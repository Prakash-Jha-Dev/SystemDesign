package com.hackedagain.truecaller.entities;

import java.time.LocalDate;

import com.hackedagain.truecaller.interfaces.IProfile;

public class PhoneBookProfile implements IProfile {

    private PhoneNumber phoneNumber;
    private String name;
    private LocalDate dateOfBirth;

    public PhoneBookProfile(PhoneNumber phoneNumber, String name, LocalDate dob) {
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.dateOfBirth = dob;
    }

    @Override
    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public String getName() {
        return name;
    }

    public LocalDate getDOB() {
        return dateOfBirth;
    }
    
}
