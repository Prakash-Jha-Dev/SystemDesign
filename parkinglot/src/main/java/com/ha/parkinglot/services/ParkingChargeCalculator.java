package com.ha.parkinglot.services;

import java.util.Map;

import com.ha.parkinglot.enums.Size;
import com.ha.parkinglot.interfaces.IParkingCalculator;
import com.ha.parkinglot.interfaces.ITicket;

public class ParkingChargeCalculator implements IParkingCalculator {
    private double parkingChargePerMinute;
    private double baseCharges;
    private Map<Size, Double> sizeMultiplier;

    public ParkingChargeCalculator(double parkingChargePerMinute, double baseCharges, Map<Size, Double> sizeMultiplier) {
        this.baseCharges = baseCharges;
        this.parkingChargePerMinute = parkingChargePerMinute;
        this.sizeMultiplier = sizeMultiplier;
    }

    public void updateBaseCharges(double newBaseCharge) {
        this.baseCharges = newBaseCharge;
    }

    @Override
    public double calculate(ITicket ticket) {
        return (ticket.getCheckoutTime().getMinute() - ticket.getCreationTime().getMinute()) 
            * parkingChargePerMinute * sizeMultiplier.get(ticket.getVehicle().getSpaceRequirements()) + baseCharges;
    }
    
}
