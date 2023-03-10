package com.ha.parkinglot.entities;

import com.ha.parkinglot.enums.Size;
import com.ha.parkinglot.interfaces.IVehicle;

public class Car implements IVehicle {

    private Size size;
    private String registrationId;

    public Car(Size size, String registrationId) {
        this.size = size;
        this.registrationId = registrationId;
    }
    @Override
    public String getRegistrationId() {
        return registrationId;
    }

    @Override
    public Size getSpaceRequirements() {
        return size;
    }
    
}
