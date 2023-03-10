package com.ha.parkinglot.services;

import java.util.ArrayList;
import java.util.List;

import com.ha.parkinglot.enums.Status;
import com.ha.parkinglot.interfaces.ISelectionStrategy;
import com.ha.parkinglot.interfaces.ISlot;
import com.ha.parkinglot.interfaces.IVehicle;

public class TicketSelectionService implements ISelectionStrategy {

    private final int MAX_SELECTION_LIMIT = 1;

    @Override
    public List<ISlot> suggestSlotForParking(IVehicle vehicle, List<ISlot> slots) {
        List<ISlot> selectedSlots = new ArrayList<ISlot>();
        for( ISlot slot : slots ) {
            if(slot.getStatus() == Status.FREE && slot.getSlotSize().equals(vehicle.getSpaceRequirements())) {
                selectedSlots.add(slot);

                if(selectedSlots.size() >= MAX_SELECTION_LIMIT) {
                    break;
                }
            }
        }
        return selectedSlots;
    }
}
