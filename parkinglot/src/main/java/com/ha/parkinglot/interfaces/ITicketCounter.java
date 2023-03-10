package com.ha.parkinglot.interfaces;

import java.util.List;

public interface ITicketCounter {
    public void freeSlots(List<ISlot> slots) throws Exception;
    public ITicket bookParkingSlot(IVehicle vehicle);
    public List<ITicket> bookSlotsForParking(List<ISlot> slots, IVehicle vehicle) throws Exception;
}
