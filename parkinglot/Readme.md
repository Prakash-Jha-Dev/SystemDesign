# Parking Lot
Parking Lot application is used to manage parking space. 

## Features
The primary features of any parking lot application are:
- Suggest/Book a parking slot for a vehicle.
- Generate receipt at checkout.

Some good to have features are as follows:
- View status of all Parking Slots.
- Manually book preferred slot(s) for vehicle(s).

## Low Level Design
Parking Lot application can be broken down into several components for ease of understanding as well as maintenance or feature addition. 
The following design uses the building block provided by interfaces to build a parking lot application. The primary components are :
- IParkingLot : Provides the interface to main functional requirements.
- ITicketCounter : Component aiding IParkingLot to book tickets and manage parking slots.
- IParkingCalculator : Component aiding IParkingLot to calculate pricing for a booked slot.

The design in consideration also contains following classes to handle various reponsibilities.
- ISelectionStrategy : Algorithm to assign parking slot can be configured here.
- ISlot : Provides various functions to manage or know status of individual parking space.
- ITicket : Stores details of vehicle and parking space assigned alongwith check-in and check-out time.
- IPaymentService : A sample interface demonstrating use of PaymentService to complete financial transaction for an associated Ticket.
- IPaymentReceipt : Store/Print required details to produce a bill for customer.

![Class Diagram](/parkinglot/src/main/resources/ParkingLot.png)

## Design principles
- Seat selection logic should be handled separately and could be choosen at runtime.
- Handover responsibility of payment to payment service.
- Use interface for interaction to make system loosely coupled.
- Use Dependency Inversion to allow construction of objects in controlled manner.
- Use classes and structures which would allow concurrent processing. 
- Existing service classes can be added to spring bean and achieve features provided by singleton and flyweight design pattern.

## Main functions documentation
- public ITicket requestParkingSpace(IVehicle vehicle)
  - Generate a ticket with assigned slot details for a vehicle requesting parking.
  
- public double generateParkingCharges(ITicket ticket)
  - Generates the amount to be paid for availaing parking facility.
  
- public IPaymentReceipt checkout(ITicket ticket, IPaymentService paymentService)
  - Generate a receipt during checkout against a ticket using the payment method selected by customer for paying bill.
  
- public void freeSlots(List<ISlot> slots)
  - Used by system to free a slot after checkout.
  
- public List<ITicket> bookSlotsForParking(List<ISlot> slots, IVehicle vehicle)
  - Can be used to book multiple slots against some vehicle. e.g. A limo can used multiple parking slot and be parked even though system doesn't support limo parking due to size constraint.
  
- public List<ITicket> getAllTicketsForVehicle(IVehicle vehicle)
  - Ticket Counter person can get list of all tickets assigned to vehicle checking out.
  
## Test Cases
 Integration test covering following scenarios are available.
 - Book a parking slot for a vehicle.
 - Fail to book when parking slot doesn't support vehicle space requirements.
 - Book a parking slot and perform checkout.
 - Fail to book slot when it is occupied by another vehicle.
 - Successfully book slot of users preference when it is free.
  
 ## Additional Details
 Code is written in Java (17) and uses Gradle for build. SpringBoot framework is used to keep code simple and easily extend to use ORM/Database/APIs.
 
  
  
