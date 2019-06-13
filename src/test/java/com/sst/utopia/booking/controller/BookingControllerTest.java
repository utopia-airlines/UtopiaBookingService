package com.sst.utopia.booking.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.WebApplicationContext;

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
 * Test of the booking controller.
 * @author Jonathan Lovelace
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class BookingControllerTest {
	@Autowired
    private WebApplicationContext context;

	private MockMvc mvc;

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
	 * Set up sample data the booking service can operate on.
	 */
	@BeforeEach
	public void init() {
		mvc = MockMvcBuilders.webAppContextSetup(context).build();
		airportDao.save(new Airport("QQQ", "Sample Airport One"));
		airportDao.save(new Airport("QQX", "Sample Airport Two"));
		userDao.save(new User(1, "sampleUser", "Sample User", "sample@example.com",
				"5555555555"));
		userDao.save(new User(2, "sampleUser2", "Second User", "second@example.com",
				"5555555556"));
		flightDao.save(new Flight(1, airportDao.findById("QQQ").get(),
				LocalDateTime.now().plusDays(4), airportDao.findById("QQX").get(),
				LocalDateTime.now().plusDays(6), 152));
		ticketDao.save(new Ticket(
				new SeatLocation(flightDao.findByFlightNumber(152).get(0), 1, "A"),
				1));
	}

	@Test
	public void testBookTicket() throws Exception {
		mvc.perform(post("/booking/book/152/1/A/")
				.contentType(MediaType.APPLICATION_JSON).content("{\"id\": 1}"))
				.andExpect(status().isCreated());
		mvc.perform(post("/booking/book/152/1/A/")
				.contentType(MediaType.APPLICATION_JSON).content("{\"id\": 2}"))
				.andExpect(status().isConflict());
	}

	@Test
	public void testAcceptPayment() throws Exception {
		mvc.perform(put("/booking/pay/152/1/A")
				.contentType(MediaType.APPLICATION_JSON).content("{\"price\":300}"))
				.andExpect(status().isGone());
		mvc.perform(post("/booking/book/152/1/A/")
				.contentType(MediaType.APPLICATION_JSON).content("{\"id\":1}"));
		mvc.perform(put("/booking/pay/152/1/A")
				.contentType(MediaType.APPLICATION_JSON).content("{\"price\":300}"))
				.andExpect(status().isOk());
		mvc.perform(put("/booking/pay/152/1/A")
				.contentType(MediaType.APPLICATION_JSON).content("{\"price\":300}"))
				.andExpect(status().isOk()); // testing idempotency
		mvc.perform(put("/booking/pay/152/1/A")
				.contentType(MediaType.APPLICATION_JSON).content("{\"price\":400}"))
				.andExpect(status().isConflict());
	}

	@Test
	public void testAcceptPaymentByBookingId() throws Exception {
		final String bookingId = DigestUtils.md5DigestAsHex("152 1 A 1".getBytes());
		mvc.perform(put("/booking/pay/" + bookingId)
				.contentType(MediaType.APPLICATION_JSON).content("{\"price\":300}"))
				.andExpect(status().isGone());
		mvc.perform(post("/booking/book/152/1/A/")
				.contentType(MediaType.APPLICATION_JSON).content("{\"id\":1}"))
				.andExpect(jsonPath("$.bookingId", is(bookingId)));
		mvc.perform(put("/booking/pay/" + bookingId)
				.contentType(MediaType.APPLICATION_JSON).content("{\"price\":300}"))
				.andExpect(status().isOk());
		mvc.perform(put("/booking/pay/" + bookingId)
				.contentType(MediaType.APPLICATION_JSON).content("{\"price\":300}"))
				.andExpect(status().isOk()); // testing idempotency
		mvc.perform(put("/booking/pay/" + bookingId)
				.contentType(MediaType.APPLICATION_JSON).content("{\"price\":400}"))
				.andExpect(status().isConflict());
	}
}
