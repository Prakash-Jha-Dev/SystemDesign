package com.ha.parkinglot.interfaces;

import com.ha.parkinglot.enums.Size;
import com.ha.parkinglot.enums.Status;

public interface ISlot {
    public int getSlotId();
    public int getFloorId();
    public Status getStatus();
    public void setStatus(Status value);
    public Size getSlotSize();
    public ITicket getAssignedTicket();
    public void setAssignedTicket(ITicket ticket) throws Exception;
    public boolean isAvailable();
    public void freeSlot() throws Exception;
    
}
