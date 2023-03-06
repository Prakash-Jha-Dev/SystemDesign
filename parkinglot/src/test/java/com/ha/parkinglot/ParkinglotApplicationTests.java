package com.ha.parkinglot;

import com.ha.parkinglot.entities.Car;
import com.ha.parkinglot.entities.PaymentReceipt;
import com.ha.parkinglot.entities.Slot;
import com.ha.parkinglot.entities.Ticket;
import com.ha.parkinglot.enums.Size;
import com.ha.parkinglot.interfaces.*;
import com.ha.parkinglot.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ParkinglotApplicationTests {

	private ParkingLot parkingLot;
	private TicketIssueCounter ticketIssueCounter;
	private ParkingChargeCalculator parkingChargeCalculator;
	private TicketSelectionService selectionService;
	private CreditCardPayment creditCardPayment;

	@BeforeEach
	public void setup() {
		creditCardPayment = new CreditCardPayment();

		selectionService = new TicketSelectionService();

		Map<Size, Double> parkingChargesMultiplierMap = new HashMap<>();
		parkingChargesMultiplierMap.put(Size.SMALL, 1.0);
		parkingChargesMultiplierMap.put(Size.MEDIUM, 2.0);
		parkingChargesMultiplierMap.put(Size.LARGE, 3.0);
		parkingChargeCalculator = new ParkingChargeCalculator(2, 10, parkingChargesMultiplierMap);

		List<ISlot> slots = new ArrayList<>();
		for(int floor = 1; floor <=3; floor++) {
			for(int slot = 1; slot <= 100; slot++) {
				slots.add(new Slot(slot, floor, Size.SMALL));
			}
			for(int slot = 101; slot <= 150; slot++) {
				slots.add(new Slot(slot, floor, Size.MEDIUM));
			}
			for(int slot = 150; slot <= 180; slot++) {
				slots.add(new Slot(slot, floor, Size.LARGE));
			}
		}
		ticketIssueCounter = new TicketIssueCounter(slots, selectionService);

		parkingLot = new ParkingLot(ticketIssueCounter, parkingChargeCalculator);
	}

	@Test
	public void testTicketBooking() {
		Car car = new Car(Size.MEDIUM, "IN-1234");
		Ticket ticket = (Ticket) parkingLot.requestParkingSpace(car);
		assertTrue(ticket.getTicketId() != null);
	}

	@Test
	public void testParkingSpaceNotAvailable() {
		Car car = new Car(Size.EXTRALARGE, "IN-1234");
		Ticket ticket = (Ticket) parkingLot.requestParkingSpace(car);
		assertTrue(ticket == null);
	}

	@Test
	public void testParkAndCheckout() throws Exception {
		Car car = new Car(Size.MEDIUM, "IN-1234");
		Ticket ticket = (Ticket) parkingLot.requestParkingSpace(car);
		PaymentReceipt paymentReceipt = (PaymentReceipt) parkingLot.checkout(ticket, creditCardPayment);
		assertTrue(ticket.getTicketId() != null);
		assertTrue(paymentReceipt.getTransactionId().startsWith("CreditCard"));
		assertTrue(paymentReceipt.getAmount() >= 10.0);
	}

	@Test
	public void testBookSlot() throws Exception {
		Car car = new Car(Size.MEDIUM, "IN-1234");
		Ticket ticket = (Ticket) parkingLot.requestParkingSpace(car);
		Slot slot = (Slot) ticket.geSlot();

		Car anotherCar = new Car(Size.MEDIUM, "IN-4321");
		List<ITicket> tickets = ticketIssueCounter.bookSlotsForParking(Arrays.asList(slot), car);
		assertTrue(tickets.size() == 0);
	}

	@Test
	public void testRebookSlotAfterCheckout() throws Exception {
		Car car = new Car(Size.MEDIUM, "IN-1234");
		Ticket ticket = (Ticket) parkingLot.requestParkingSpace(car);
		Slot slot = (Slot) ticket.geSlot();

		Car anotherCar = new Car(Size.MEDIUM, "IN-4321");
		parkingLot.checkout(ticket, creditCardPayment);
		List<ITicket> tickets = ticketIssueCounter.bookSlotsForParking(Arrays.asList(slot), car);
		assertTrue(tickets.size() == 1);
		assertTrue(tickets.get(0).geSlot().getSlotId() == slot.getSlotId());
	}
}
