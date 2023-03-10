package com.ha.parkinglot.interfaces;

import java.time.LocalDateTime;

public interface ITicket {
    public String getTicketId();
    public ISlot geSlot();
    public LocalDateTime getCreationTime();
    public LocalDateTime getCheckoutTime();
    public IVehicle getVehicle();
    public void setCheckoutTime();
}
