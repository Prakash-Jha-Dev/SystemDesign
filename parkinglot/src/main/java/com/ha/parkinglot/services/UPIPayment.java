package com.ha.parkinglot.services;

import java.util.UUID;

import com.ha.parkinglot.interfaces.IPaymentService;

public class UPIPayment implements IPaymentService {

    @Override
    public String pay(double amt) {
        System.out.println( String.format("Paying amount of %D via UPI", amt) );
        return "UPI:"+UUID.randomUUID();
    }
    
}
