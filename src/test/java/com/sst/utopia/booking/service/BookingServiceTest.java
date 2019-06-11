package com.sst.utopia.booking.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.sst.utopia.booking.dao.AirportDao;
import com.sst.utopia.booking.dao.FlightDao;
import com.sst.utopia.booking.dao.TicketDao;
import com.sst.utopia.booking.dao.UserDao;
import com.sst.utopia.booking.model.Airport;
import com.sst.utopia.booking.model.AirportDateDTO;
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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
	@BeforeAll
	public void init() {
		airportDao.save(new Airport("QQQ", "Sample Airport One"));
		airportDao.save(new Airport("QQX", "Sample Airport Two"));
		userDao.save(new User(1, "sampleUser", "Sample User", "sample@example.com",
				"5555555555"));
		flightDao.save(new Flight(1,
				new AirportDateDTO(airportDao.getOne("QQQ"),
						LocalDateTime.now().plusDays(4)),
				new AirportDateDTO(airportDao.getOne("QQX"),
						LocalDateTime.now().plusDays(6)), 152));
		ticketDao.save(new Ticket(
				new SeatLocation(flightDao.findByFlightNumber(152).get(0), 1, "A"),
				1));
	}

	@Test
	public void testBookTicket() {
		final SeatLocation seat = new SeatLocation(
				flightDao.findByFlightNumber(152).get(0), 1, "A");
		assertNull(ticketDao.getOne(seat).getReserver(),
				"Ticket starts out unbooked");
		testee.bookTicket(seat, userDao.getOne(1)); // TODO: Make getByUsername(), getByEmail(), getByPhone()
		assertNotNull(ticketDao.getOne(seat).getReserver(),
				"Ticket is reserved after booking");
		assertThrows(IllegalArgumentException.class,
				() -> testee.bookTicket(seat, userDao.getOne(1)),
				"Can't book already-booked ticket");
		testee.cancelPendingReservation(ticketDao.getOne(seat));
	}

	@Test
	public void testAcceptPayment() {
		final SeatLocation seat = new SeatLocation(
				flightDao.findByFlightNumber(152).get(0), 1, "A");
		assertThrows(IllegalArgumentException.class,
				() -> testee.acceptPayment(ticketDao.getOne(seat), 150),
				"Can't pay for unbooked ticket");
		final Ticket ticket = testee.bookTicket(seat, userDao.getOne(1));
		assertNull(ticketDao.getOne(seat).getPrice(), "Price not set after booking");
		testee.acceptPayment(ticket, 300);
		assertEquals(300, ticketDao.getOne(seat).getPrice(), "Price set after paying");
		assertNull(ticketDao.getOne(seat).getReservationTimeout(),
				"Reservation timeout gone after paying");
		ticket.setReserver(null);
		ticketDao.saveAndFlush(ticket);
	}

	@Test
	public void testCancelPendingReservation() {
		final SeatLocation seat = new SeatLocation(
				flightDao.findByFlightNumber(152).get(0), 1, "A");
		final Ticket ticket = testee.bookTicket(seat, userDao.getOne(1));
		assertNotNull(ticketDao.getOne(seat).getReserver(),
				"Seat reserved after booking");
		testee.cancelPendingReservation(ticket);
		assertNull(ticketDao.getOne(seat).getReserver(),
				"Seat no longer reserved after cancelling");
		testee.cancelPendingReservation(ticket); // test idempotency of cancellation
	}

	@Test
	public void testPayUsingBookingId() {
		final SeatLocation seat = new SeatLocation(
				flightDao.findByFlightNumber(152).get(0), 1, "A");
		final Ticket ticket = testee.bookTicket(seat, userDao.getOne(1));
		assertNull(ticketDao.getOne(seat).getPrice(), "Price not set after booking");
		testee.acceptPayment(ticket.getBookingId(), 300);
		assertEquals(300, ticketDao.getOne(seat).getPrice(),
				"Price set after paying with booking ID");
		ticket.setReserver(null);
		ticketDao.saveAndFlush(ticket);
	}

	@Test
	public void testCancelUsingBookingId() {
		final SeatLocation seat = new SeatLocation(
				flightDao.findByFlightNumber(152).get(0), 1, "A");
		final Ticket ticket = testee.bookTicket(seat, userDao.getOne(1));
		assertNotNull(ticketDao.getOne(seat).getReserver(),
				"Seat reserved after booking");
		testee.cancelPendingReservation(ticket.getBookingId());
		assertNull(ticketDao.getOne(seat).getReserver(),
				"Seat no longer reserved after cancelling using booking ID");
	}
}
