package com.ha.parkinglot.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ha.parkinglot.interfaces.ISlot;
import com.ha.parkinglot.interfaces.ITicket;
import com.ha.parkinglot.interfaces.IVehicle;

public class Ticket implements ITicket {

    private final String ticketId;
    private final ISlot slot;
    private final LocalDateTime creationTime;
    private final IVehicle vehicle;
    private LocalDateTime checkoutTime;

    public Ticket(ISlot slot, IVehicle vehicle) {
        this.ticketId = UUID.randomUUID().toString();
        this.slot = slot;
        this.vehicle = vehicle;
        this.creationTime = LocalDateTime.now();
    }

    @Override
    public String getTicketId() {
        return ticketId;
    }

    @Override
    public ISlot geSlot() {
        return slot;
    }

    @Override
    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    @Override
    public IVehicle getVehicle() {
        return vehicle;
    }
    
    @Override
    public LocalDateTime getCheckoutTime() {
        return checkoutTime;
    }

    @Override
    public synchronized void setCheckoutTime() {
        if (checkoutTime == null) {
            checkoutTime = LocalDateTime.now();
        }
    }
}
