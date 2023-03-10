package com.ha.parkinglot.services;

import java.util.UUID;

import com.ha.parkinglot.interfaces.IPaymentService;

public class CreditCardPayment implements IPaymentService {
    
    @Override
    public String pay(double amt) {
        System.out.println( String.format("Paying amount of %f via creditCard", amt) );
        return "CreditCard:"+UUID.randomUUID();
    }


}
