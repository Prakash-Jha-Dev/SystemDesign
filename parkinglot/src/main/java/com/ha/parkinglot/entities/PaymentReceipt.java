package com.ha.parkinglot.entities;

import java.time.LocalDateTime;

import com.ha.parkinglot.interfaces.IPaymentReceipt;
import com.ha.parkinglot.interfaces.ITicket;

public class PaymentReceipt implements IPaymentReceipt {
    private String ticketId;
    private double amount;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String vehicleReigstrationId;
    private String transactionId;

    public PaymentReceipt(ITicket ticket, double amount, String transId) {
        this.ticketId = ticket.getTicketId();
        this.amount = amount;
        this.checkInTime = ticket.getCreationTime();
        this.checkOutTime = ticket.getCheckoutTime();
        this.vehicleReigstrationId = ticket.getVehicle().getRegistrationId();
        this.transactionId = transId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public String getVehicleReigstrationId() {
        return vehicleReigstrationId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PaymentReceipt{");
        sb.append("ticketId='").append(ticketId).append('\'');
        sb.append(", amount=").append(amount);
        sb.append(", checkInTime=").append(checkInTime);
        sb.append(", checkOutTime=").append(checkOutTime);
        sb.append(", vehicleReigstrationId='").append(vehicleReigstrationId).append('\'');
        sb.append(", transactionId='").append(transactionId).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public String print() {
        return toString();
    }
}
