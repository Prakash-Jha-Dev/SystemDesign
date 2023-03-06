package com.ha.parkinglot.entities;

import com.ha.parkinglot.enums.Size;
import com.ha.parkinglot.enums.Status;
import com.ha.parkinglot.interfaces.ISlot;
import com.ha.parkinglot.interfaces.ITicket;

public class Slot implements ISlot {

    private final int slotId;
    private final int floorId;
    private final Size size;
    private ITicket assignedToTicket;
    private Status status;

    public Slot(int slotId, int floorId, Size size) {
        this.slotId = slotId;
        this.floorId = floorId;
        this.size = size;
        this.status = Status.FREE;
    }

    @Override
    public int getSlotId() {
        return slotId;
    }

    @Override
    public int getFloorId() {
        return floorId;
    }

    @Override
    public Size getSlotSize() {
        return size;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public synchronized void setStatus(Status value) {
        status = value;
    }

    @Override
    public ITicket getAssignedTicket() {
        return assignedToTicket;
    }

    @Override
    public synchronized void setAssignedTicket(ITicket ticket) throws Exception {
        if(!this.isAvailable()) {
            throw new Exception("Slot is not available: " + this);
        }
        this.assignedToTicket = ticket;
        this.setStatus(Status.OCCUPIED);
    }

    @Override
    public synchronized void freeSlot() throws Exception {
        if (this.getAssignedTicket() != null && this.getAssignedTicket().getCheckoutTime() == null) {
            throw new Exception("Slot is occupied by vehicle: "+ this.getAssignedTicket().getVehicle().getRegistrationId());
        }
        this.setStatus(Status.FREE);
        this.assignedToTicket = null;
    }

    @Override
    public boolean isAvailable() {
        return this.getStatus() == Status.FREE;
    }
    
}
