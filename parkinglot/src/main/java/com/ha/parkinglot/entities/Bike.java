package com.ha.parkinglot.entities;

import com.ha.parkinglot.enums.Size;
import com.ha.parkinglot.interfaces.IVehicle;

public class Bike implements IVehicle {

    private Size size;
    private String registrationId;

    @Override
    public String getRegistrationId() {
        return registrationId;
    }

    @Override
    public Size getSpaceRequirements() {
        return size;
    }
    
}
