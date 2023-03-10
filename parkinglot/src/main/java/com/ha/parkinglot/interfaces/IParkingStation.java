package com.ha.parkinglot.interfaces;

public interface IParkingStation {
    public ITicket requestParkingSpace(IVehicle vehicle);
    public double generateParkingCharges(ITicket ticket);
    public IPaymentReceipt checkout(ITicket ticket, IPaymentService paymentService) throws Exception;
}
