package com.sst.utopia.booking.controller;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sst.utopia.booking.model.PaymentAmount;
import com.sst.utopia.booking.model.SeatLocation;
import com.sst.utopia.booking.model.Ticket;
import com.sst.utopia.booking.model.User;
import com.sst.utopia.booking.service.BookingService;

/**
 * Controller to present the booking service to the microservices that provide
 * the client-facing API.
 *
 * @author Jonathan Lovelace
 */
@RestController
@RequestMapping("/booking")
public class BookingController {
	/**
	 * Service class used to handle requests.
	 */
	@Autowired
	private BookingService service;
	/**
	 * Reserve a ticket for the given seat.
	 * FIXME: Allow getting the user from headers (injected by the security layer)
	 * @param flight the flight number of the flight
	 * @param row the row number of the seat
	 * @param seat the seat within the row
	 * @param user the user details
	 */
	@PostMapping("/book/{flight}/{row}/{seat}")
	public ResponseEntity<Ticket> bookTicket(@PathVariable final int flight,
			@PathVariable final int row, @PathVariable final String seat,
			@RequestBody final User user) {
		try {
			return new ResponseEntity<>(service.bookTicket(
					new SeatLocation(service.getFlight(flight), row, seat), user),
					HttpStatus.CREATED);
		} catch (final IllegalArgumentException except) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		} catch (final Exception except) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	/**
	 * Accept payment for a given reserved seat.
	 * @param flight the flight number of the flight
	 * @param row the row number of the seat
	 * @param seat the seat within the row
	 * @param payment the price the customer has paid for the ticket
	 */
	@PutMapping("/pay/{flight}/{row}/{seat}")
	public ResponseEntity<Ticket> acceptPayment(@PathVariable final int flight,
			@PathVariable final int row, @PathVariable final String seat,
			@RequestBody final PaymentAmount payment) {
		try {
			service.acceptPayment(
					service.getTicket(
							new SeatLocation(service.getFlight(flight), row, seat)),
					payment.getPrice());
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (final IllegalStateException except) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		} catch (final IllegalArgumentException except) {
			return new ResponseEntity<>(HttpStatus.GONE);
		} catch (final NoSuchElementException except) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (final Exception except) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Accept payment for a given reserved seat.
	 * @param bookingId the ID code of the booking
	 * @param payment the price the customer has paid for the ticket
	 */
	@PutMapping("/pay/{bookingId}")
	public ResponseEntity<Ticket> acceptPaymentForBookingId(
			@PathVariable final String bookingId,
			@RequestBody final PaymentAmount payment) {
		try {
			return new ResponseEntity<>(
					service.acceptPayment(bookingId, payment.getPrice()),
					HttpStatus.OK);
		} catch (final IllegalArgumentException except) {
			return new ResponseEntity<>(HttpStatus.GONE);
		} catch (final IllegalStateException except) {
			if (except.getMessage().contains("Uniqueness")) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
				return new ResponseEntity<>(HttpStatus.CONFLICT);
			}
		} catch (final Exception except) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
