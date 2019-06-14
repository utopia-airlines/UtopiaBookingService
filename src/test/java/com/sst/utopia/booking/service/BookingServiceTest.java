package com.sst.utopia.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.sst.utopia.booking.dao.AirportDao;
import com.sst.utopia.booking.dao.FlightDao;
import com.sst.utopia.booking.dao.TicketDao;
import com.sst.utopia.booking.dao.UserDao;
import com.sst.utopia.booking.model.Airport;
import com.sst.utopia.booking.model.Flight;
import com.sst.utopia.booking.model.SeatLocation;
import com.sst.utopia.booking.model.Ticket;
import com.sst.utopia.booking.model.User;

/**
 * Tests of the booking service class.
 *
 * @author Jonathan Lovelace
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class BookingServiceTest {
	/**
	 * Airport DAO used in tests.
	 */
	@Autowired
	private AirportDao airportDao;
	/**
	 * User DAO used in tests.
	 */
	@Autowired
	private UserDao userDao;
	/**
	 * Flight DAO used in tests.
	 */
	@Autowired
	private FlightDao flightDao;
	/**
	 * Ticket DAO used in tests.
	 */
	@Autowired
	private TicketDao ticketDao;
	/**
	 * Object under test.
	 */
	@Autowired
	private BookingService testee;
	/**
	 * Set up sample data the booking service can operate on.
	 */
	@BeforeEach
	public void init() {
		airportDao.save(new Airport("QQQ", "Sample Airport One"));
		airportDao.save(new Airport("QQX", "Sample Airport Two"));
		userDao.save(new User(1, "sampleUser", "Sample User", "sample@example.com",
				"5555555555"));
		flightDao.save(new Flight(1,
				airportDao.findById("QQQ").get(),
						LocalDateTime.now().plusDays(4),
				airportDao.findById("QQX").get(),
						LocalDateTime.now().plusDays(6), 152));
		ticketDao.save(new Ticket(
				new SeatLocation(flightDao.findByFlightNumber(152).get(0), 1, "A"),
				1));
	}

	@Test
	public void testBookTicket() {
		final SeatLocation seat = new SeatLocation(
				flightDao.findByFlightNumber(152).get(0), 1, "A");
		assertFalse(ticketDao.findById(seat).map(Ticket::getReserver).isPresent(),
				"Ticket starts out unbooked");
		testee.bookTicket(seat, userDao.findById(1).get()); // TODO: Make getByUsername(), getByEmail(), getByPhone()
		assertTrue(ticketDao.findById(seat).map(Ticket::getReserver).isPresent(),
				"Ticket is reserved after booking");
		assertThrows(IllegalArgumentException.class,
				() -> testee.bookTicket(seat, userDao.findById(1).get()),
				"Can't book already-booked ticket");
		testee.cancelPendingReservation(ticketDao.findById(seat).get());
	}

	@Test
	public void testAcceptPayment() {
		final SeatLocation seat = new SeatLocation(
				flightDao.findByFlightNumber(152).get(0), 1, "A");
		assertThrows(IllegalArgumentException.class,
				() -> testee.acceptPayment(ticketDao.findById(seat).get(), 150),
				"Can't pay for unbooked ticket");
		final Ticket ticket = testee.bookTicket(seat, userDao.findById(1).get());
		assertFalse(ticketDao.findById(seat).map(Ticket::getPrice).isPresent(),
				"Price not set after booking");
		testee.acceptPayment(ticket, 300);
		assertEquals(300, ticketDao.findById(seat).map(Ticket::getPrice).get(),
				"Price set after paying");
		assertFalse(ticketDao.findById(seat).map(Ticket::getReservationTimeout)
				.isPresent(), "Reservation timeout gone after paying");
		ticket.setReserver(null);
		ticketDao.saveAndFlush(ticket);
	}

	@Test
	public void testCancelPendingReservation() {
		final SeatLocation seat = new SeatLocation(
				flightDao.findByFlightNumber(152).get(0), 1, "A");
		final Ticket ticket = testee.bookTicket(seat, userDao.findById(1).get());
		assertTrue(ticketDao.findById(seat).map(Ticket::getReserver).isPresent(),
				"Seat reserved after booking");
		testee.cancelPendingReservation(ticket);
		assertFalse(ticketDao.findById(seat).map(Ticket::getReserver).isPresent(),
				"Seat no longer reserved after cancelling");
		testee.cancelPendingReservation(ticket); // test idempotency of cancellation
	}

	@Test
	public void testPayUsingBookingId() {
		final SeatLocation seat = new SeatLocation(
				flightDao.findByFlightNumber(152).get(0), 1, "A");
		final Ticket ticket = testee.bookTicket(seat, userDao.findById(1).get());
		assertFalse(ticketDao.findById(seat).map(Ticket::getPrice).isPresent(),
				"Price not set after booking");
		testee.acceptPayment(ticket.getBookingId(), 300);
		assertEquals(300, ticketDao.findById(seat).map(Ticket::getPrice).get(),
				"Price set after paying with booking ID");
		ticket.setReserver(null);
		ticketDao.saveAndFlush(ticket);
	}

	@Test
	public void testCancelUsingBookingId() {
		final SeatLocation seat = new SeatLocation(
				flightDao.findByFlightNumber(152).get(0), 1, "A");
		final Ticket ticket = testee.bookTicket(seat, userDao.findById(1).get());
		assertTrue(ticketDao.findById(seat).map(Ticket::getReserver).isPresent(),
				"Seat reserved after booking");
		testee.cancelPendingReservation(ticket.getBookingId());
		assertFalse(ticketDao.findById(seat).map(Ticket::getReserver).isPresent(),
				"Seat no longer reserved after cancelling using booking ID");
	}
}
