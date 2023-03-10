package com.ha.parkinglot.interfaces;

import java.util.List;

public interface ISelectionStrategy {
    public List<ISlot> suggestSlotForParking(IVehicle vehicle, List<ISlot> slots);
}
