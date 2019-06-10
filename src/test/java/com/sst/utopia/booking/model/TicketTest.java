package com.sst.utopia.booking.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/**
 * Test case to ensure that Ticket invariant-validation code works properly.
 *
 * @author Jonathan Lovelace
 */
public class TicketTest {

	@Test
	public final void testClearingReserverClearsPrice() {
		final Ticket ticket = new Ticket();
		ticket.setReserver(new User());
		ticket.setPrice(500);
		ticket.setReserver(null);
		assertNull(ticket.getPrice(), "Setting reserver to null sets price to null");
	}

	@Test
	public final void testClearingReserverClearsTimeout() {
		final Ticket ticket = new Ticket();
		ticket.setReserver(new User());
		ticket.setReservationTimeout(LocalDateTime.now());
		ticket.setReserver(null);
		assertNull(ticket.getReservationTimeout(),
				"Setting reserver to null sets reservation timeout to null");
	}

	@Test
	public final void testClearingReserverClearsBookingId() {
		final Ticket ticket = new Ticket();
		ticket.setReserver(new User());
		ticket.setBookingId("abc123");
		ticket.setReserver(null);
		assertNull(ticket.getBookingId(),
				"Setting reserver to null sets booking ID to null");
	}

	@Test
	public final void testNoPriceWithoutReservation() {
		assertThrows(IllegalStateException.class, () -> new Ticket().setPrice(500),
				"Can't set price-paid for an unbooked ticket");
	}

	@Test
	public final void testNoTimeoutWithoutReservation() {
		assertThrows(IllegalStateException.class,
				() -> new Ticket().setReservationTimeout(LocalDateTime.now()),
				"Can't set reservation timeout for an unbooked ticket");
	}

	@Test
	public final void testNoTimeoutForConfirmedTicket() {
		final Ticket ticket = new Ticket();
		ticket.setReserver(new User());
		ticket.setPrice(150);
		assertThrows(IllegalStateException.class,
				() -> ticket.setReservationTimeout(LocalDateTime.now()),
				"Can't set timeout for an already-paid ticket");
	}

	@Test
	public final void testIsValid() {
		final Ticket ticket = new Ticket();
		assertTrue(ticket.isValid(), "No reserver, no price, no timeout is valid");
		ticket.setReserver(new User());
		assertFalse(ticket.isValid(), "Reserved ticket without booking ID is not valid");
		ticket.setBookingId("bookingId");
		assertFalse(ticket.isValid(),
				"Reserved ticket with neither price nor timeout is not valid");
		ticket.setPrice(300);
		assertTrue(ticket.isValid(), "Reserved ticket with price but no timeout is valid");
		ticket.setPrice(null);
		ticket.setReservationTimeout(LocalDateTime.now());
		assertTrue(ticket.isValid(), "Reserved ticket with timeout but no price is valid");
	}
}
