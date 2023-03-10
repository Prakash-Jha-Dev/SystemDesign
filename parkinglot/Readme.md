# Overview of a Parking Lot
Parking Lot Application is used at ticket counter of any parking space facility. A parking lot facility can have multiple entry and exit points hence there can be multiple counters to keep track of available parking space and information on vehicles parked.

## Functional Requirements
The main features required by a parking lot application are:
1. Suggest a parking space for a vehicle.
2. Generate a ticket for a vehicle availing parking facility.
3. During exit of vehicle from parking facility, charge parking amount and issue a Payment Receipt.

## Non functional Requirements
Apart from above features, some additional features are required or good-to-have:
1. A parking lot system should be able to manage a parking facility having multiple entry/exit i.e. System should be distributed with concurrent booking and checkout.
2. The system should store details of vehicle using Parking Lot facility i.e. Persistent storage for recovery and auditing.
3. Ticket counter should be able to quickly issue ticket as well as perform checkout i.e. Performant and Available.
4. The details stored by parking lot should be accurate i.e. At-least eventually consistent with some restriction logic tied to application or strongly consistent.

## APIs
On basis of above, we can list functions or APIs required by system for interaction.
1. requestParkingSpace(Vehicle) or /requestParkingSpace/{vehicle} > returns a ticket with slot details.
2. checkout(Ticket, Payment) or /checkout/ {ticket and payment as post form data}

## Back-of-the-envelope calculation
Assuming a Parking facility has about 500 parking space providing 24x7 service and each slot is occupied by approx. 12 vehicles in a day. The tickets issued throughout day would be 500 * 12 = 6000. The application needs to handle details of 500 Parking Slots actively and store about 3 years of previous data passively (for audit). It means that system needs to have a storage for 600 * 365 * 3 = 657000 tickets alongwith a storage to manage 500 slots actively.

Each ticket would store following information:
Vehicle registration ID (10 characters)
ParkingSlot Id (Integer)
CheckInTime (Date Time)
CheckOutTime (Date Time)

From above, storing tickets for 3 years equate to ~657000 * (10 + 4 + 8 + 8) = ~ 20 GB. System might store additional information such as payment mode, amount, transactionId. Roughly estimating, archive data storage turns out to 20 GB * 3 = 60 GB. Data would probably be stored on different storage system to allow recovery (Disk failure, data corruption, handling damage, etc. scenarios) . A 3 times replication strategy requires total storage of 180 GB (60GB on each storage).

System also needs to manage 500 slots actively i.e., quickly assign a vehicle to parking lot, manage a ticket generate on just parked vehcile, etc. This data should be stored in-memory for fast access with a Write-Ahead-Log used to flush operations to disk (for recovery). Since, these slots are managed from different counters in a concurrent manner, each instance of application should sync with a database to fetch latest status. It should be noted that all these systems are present within a small physical space and hence can communicate extremely fast leading to quick sync. Moreover, since the resource requirements are less, the in-memory database along with instance of application can be placed on the same physical host to reduce one hop in communication.

## Database
The amount of slots are extremely less, so perforamce shouldn't be a challenge and network isn't a challenge either since the setup is similar to a datacenter (Maybe, we can even store the data in cloud for archival. Archival process can be managed separately.).

For the active slots data, we probably want to store information about Slot, Vehicles and Tickets. Since, we want to figure out available slot for a vehicle or inquire about a slot/vehcile/ticket details or inquire about vehcile and slot from details available from a ticket.

Many different database can be configured to achieve the required functionality. However, since I intend to break down active storage data into different tables and connect them as per their relationship, I'd prefer to use relational database such as MySQL or PostgreSQL. (It should be most easy to setup and configure for this use-case). A master-slave or even a master-master configuration would work out fine.

The data (i.e. tables) to be stored have already been mentioned. More details are as follow:

- Slot
	- slotId, ticketId, state, size
- Vehicle
	- vehicleRegistrationId, size
- Ticket
	- ticketId, vehicleRegistrationId, slotId, checkIn, checkOut
- PaymentReceipt
	- transactionId, ticketId

The archival database can be HDFS with data written into different files corresponding to each table and hive can be used for queries. Data can also be denormalized and stored together as Ticket{Slot,Vehicle,Timings,etc.}.

## HLD diagram
Each server is a monolith architecture with replicas across different ticket counters. Archival Process can be created as microservice architecture or as added into monolith server (to save cost).
![image](https://github.com/Prakash-Jha-Dev/SystemDesign/blob/main/parkinglot/src/main/resources/ParkingLotHld.png)

 
## Caching
The in-memory database is available on same physical host and hence eliminates the need of caching. Application might still cache some data to make process faster.


## Low-level design:

### Features
The features that we've to implement are:
- Suggest/Book a parking slot for a vehicle.
- Generate receipt at checkout.

We'll also implement some additional features:
- View status of all Parking Slots.
- Manually book preferred slot(s) for vehicle(s).

###  Overview
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

![Class Diagram](https://github.com/Prakash-Jha-Dev/SystemDesign/blob/main/parkinglot/src/main/resources/ParkingLot.png)

### Design principles
- Seat selection logic should be handled separately and could be choosen at runtime.
- Handover responsibility of payment to payment service.
- Use interface for interaction to make system loosely coupled.
- Use Dependency Inversion to allow construction of objects in controlled manner.
- Use classes and structures which would allow concurrent processing. 
- Existing service classes can be added to spring bean and achieve features provided by singleton and flyweight design pattern.

### Main functions documentation
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
  
### Test Cases
 Integration test covering following scenarios are available.
 - Book a parking slot for a vehicle.
 - Fail to book when parking slot doesn't support vehicle space requirements.
 - Book a parking slot and perform checkout.
 - Fail to book slot when it is occupied by another vehicle.
 - Successfully book slot of users preference when it is free.
  
 ### Code
 Code is written in Java (17) and uses Gradle for build. SpringBoot framework is used to keep code simple and easily extend to use ORM/Database/APIs.
 
  
