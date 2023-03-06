package com.ha.parkinglot.interfaces;

import com.ha.parkinglot.enums.Size;

public interface IVehicle {
    public String getRegistrationId();
    public Size getSpaceRequirements();
}
