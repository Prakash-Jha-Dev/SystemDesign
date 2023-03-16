package com.hackedagain.truecaller.entities;

public class PhoneNumber implements Comparable<PhoneNumber> {
    private String countryCode;
    private String phoneNumber;

    public PhoneNumber(String countryCode, String number) {
        this.countryCode = countryCode;
        this.phoneNumber = number;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String get() {
        return this.countryCode + this.phoneNumber;
    }

    @Override
    public int hashCode() {
        return this.get().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        PhoneNumber other = (PhoneNumber)obj;
        return other.hashCode() == this.hashCode();
    }

    @Override
    public int compareTo(PhoneNumber arg0) {
        return this.get().compareTo(arg0.get());
    }
}
