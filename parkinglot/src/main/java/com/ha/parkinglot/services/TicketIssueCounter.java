package com.ha.parkinglot.services;

import java.util.ArrayList;
import java.util.List;

import com.ha.parkinglot.entities.Ticket;
import com.ha.parkinglot.interfaces.ISelectionStrategy;
import com.ha.parkinglot.interfaces.ISlot;
import com.ha.parkinglot.interfaces.ITicket;
import com.ha.parkinglot.interfaces.ITicketCounter;
import com.ha.parkinglot.interfaces.IVehicle;

public class TicketIssueCounter implements ITicketCounter {

    private List<ISlot> allSlots;
    private ISelectionStrategy selectionStrategy;
    private final int MAX_BOOKING_ATTEMPTS = 10;

    public TicketIssueCounter(List<ISlot> slots, ISelectionStrategy selectionStrategy) {
        this.allSlots = slots;
        this.selectionStrategy = selectionStrategy;
    }

    @Override
    public void freeSlots(List<ISlot> slots) throws Exception {        
        for(ISlot slot: slots) {
            slot.freeSlot();
        }
    }

    @Override
    public ITicket bookParkingSlot(IVehicle vehicle) {
        ITicket ticket = null;
        for(int attempt = 1; attempt <= MAX_BOOKING_ATTEMPTS; attempt++) {
            List<ISlot> slots = selectionStrategy.suggestSlotForParking(vehicle, allSlots);
            try {
                ticket = bookSlot(slots.get(0), vehicle);
                break;
            } catch (Exception e) {}
        }

        return ticket;
    }

    @Override
    public List<ITicket> bookSlotsForParking(List<ISlot> slots, IVehicle vehicle) throws Exception {
        List<ITicket> tickets = new ArrayList<>();
        for(ISlot slot: slots) {
            try {
                tickets.add(bookSlot(slot, vehicle));
            } catch(Exception e) {
                for(ITicket ticket : tickets) {
                    ticket.geSlot().freeSlot();
                }
                tickets.clear();
            }
        }
        return tickets;
    }

    public List<ITicket> getAllTicketsForVehicle(IVehicle vehicle) {
        List<ITicket> tickets = new ArrayList<>();
        for(ISlot slot: allSlots) {
            if(slot.getAssignedTicket() != null && slot.getAssignedTicket().getVehicle().getRegistrationId().equals(vehicle.getRegistrationId())) {
                tickets.add(slot.getAssignedTicket());
            }
        }
        return tickets;
    }

    private ITicket bookSlot(ISlot slot, IVehicle vehicle) throws Exception {
        ITicket ticket = new Ticket(slot, vehicle);
        slot.setAssignedTicket(ticket);
        return ticket;
    }

}
