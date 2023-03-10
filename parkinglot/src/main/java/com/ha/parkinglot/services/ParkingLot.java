package com.ha.parkinglot.services;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.ha.parkinglot.entities.PaymentReceipt;
import com.ha.parkinglot.interfaces.IParkingCalculator;
import com.ha.parkinglot.interfaces.IParkingStation;
import com.ha.parkinglot.interfaces.IPaymentReceipt;
import com.ha.parkinglot.interfaces.IPaymentService;
import com.ha.parkinglot.interfaces.ITicket;
import com.ha.parkinglot.interfaces.ITicketCounter;
import com.ha.parkinglot.interfaces.IVehicle;

public class ParkingLot implements IParkingStation {

    private ConcurrentLinkedQueue<ITicket> issuedTickets;
    private ITicketCounter ticketCounter;
    private IParkingCalculator calculator;

    public ParkingLot(ITicketCounter ticketCounter, IParkingCalculator calculator) {
        this.ticketCounter = ticketCounter;
        this.calculator = calculator;
        issuedTickets = new ConcurrentLinkedQueue<ITicket>();
    }
    
    @Override
    public ITicket requestParkingSpace(IVehicle vehicle) {
        ITicket ticket = ticketCounter.bookParkingSlot(vehicle);
        if(ticket != null) {
            issuedTickets.add(ticket);
        }
        return ticket;
    }

    @Override
    public double generateParkingCharges(ITicket ticket) {
        return calculator.calculate(ticket);
    }

    @Override
    public IPaymentReceipt checkout(ITicket ticket, IPaymentService paymentService) throws Exception {
        ticket.setCheckoutTime();
        double amount = this.generateParkingCharges(ticket);
        String transactionId = paymentService.pay(amount);
        ticketCounter.freeSlots(Arrays.asList(ticket.geSlot()));
        return new PaymentReceipt(ticket, amount, transactionId);
    }
    
}
