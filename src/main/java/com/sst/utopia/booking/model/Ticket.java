package com.sst.utopia.booking.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

/**
 * A ticket, or seat (for which a ticket may be or may have been sold), on a
 * flight.
 *
 * Unlike most model classes, several fields in this class are expected to be
 * modified as tickets are booked (and cancelled, but that's out of the scope of
 * this service), and so have setters. Other fields are not expected to be
 * modified, and so do not have setters, but are left non-final for the JPA
 * persistence system. It's possible to get an instance of this class into an
 * invalid state using the provided setters; the isValid() method is provided to
 * check validity, and callers are expected to pass only valid objects to other
 * code. But some setters do some basic sanity checks.
 *
 * @author Jonathan Lovelace
 *
 */
@Entity
@Table(name = "tbl_tickets")
public class Ticket {
	/**
	 * What flight this seat/ticket is for, and where in the plane it is.
	 */
	@EmbeddedId
	private SeatLocation id;
	/**
	 * What class of seat this is. For now first class is "1", business class is
	 * "2", and economy is "3". TODO: make an enum?
	 */
	@Column(name = "class")
	private int seatClass;
	/**
	 * The holder of this ticket, if any.
	 */
	@ManyToOne
	@JoinColumn(nullable = true, name="reserver")
	@JsonProperty(access = Access.WRITE_ONLY)
	private User reserver;
	/**
	 * The price that the ticket-holder paid to book this seat; must be null if
	 * reserver is null or if both reserver and reservationTimeout are not null, but
	 * must not be null if reserver is not null but reservationTimeout is.
	 */
	@Column(nullable = true)
	private Integer price;
	/**
	 * If not null, how long the user who reserved this seat has to confirm that
	 * reservation by paying for the ticket. Must be null if reserver is null or if
	 * both reserver and price are not null, but must not be null if reserver is not
	 * null but price is.
	 */
	@Column(nullable = true)
	private LocalDateTime reservationTimeout;

	/**
	 * The "ID" that customers can use to refer to their booking; it is (intended to
	 * be) a hash of the flight, row, seat, and reserver, and must be set when
	 * 'reserver' is set.
	 */
	@Column(nullable = true)
	private String bookingId;
	/**
	 * Default constructor for JPA.
	 */
	public Ticket() {
	}
	/**
	 * More complete constructor for tests.
	 */
	public Ticket(final SeatLocation id, final int seatClass) {
		this.id = id;
		this.seatClass = seatClass;
	}

	/**
	 * @return the flight, row, and seat that together uniquely identify this
	 *         ticket.
	 */
	public SeatLocation getId() {
		return id;
	}

	/**
	 * @return the class of the seat
	 */
	public int getSeatClass() {
		return seatClass;
	}

	/**
	 * @return the ticket-holder, if any
	 */
	public User getReserver() {
		return reserver;
	}

	/**
	 * @return the price the ticket-holder paid, if the ticket is reserved and he or
	 *         she has paid
	 */
	public Integer getPrice() {
		return price;
	}

	/**
	 * @return when the reservation will time out if the user who reserved the
	 *         ticket has not paid by then
	 */
	public LocalDateTime getReservationTimeout() {
		return reservationTimeout;
	}

	/**
	 * @return the "booking ID" shared with the ticket-holder, or null if not yet booked.
	 */
	public String getBookingId() {
		return bookingId;
	}

	/**
	 * If the ticket-holder is set to null, price and reservationTimeout are also
	 * set to null.
	 *
	 * @param reserver the new ticket-holder
	 */
	public void setReserver(final User reserver) {
		if (reserver == null) {
			price = null;
			reservationTimeout = null;
			bookingId = null;
		}
		this.reserver = reserver;
	}

	/**
	 * This method throws if the new price is not null but reserver is; if neither
	 * reserver nor the new price are null, reservationTimeout is set to null.
	 *
	 * @param price the price the ticket-holder paid for the seat
	 * @throws IllegalStateException if price is not null but reserver is
	 */
	public void setPrice(final Integer price) {
		if (reserver == null && price != null) {
			throw new IllegalStateException("Ticket can only be paid for if someone reserved it");
		} else if (reserver != null && price != null) {
			reservationTimeout = null;
		}
		this.price = price;
	}

	/**
	 * This method throws if the new timeout is not null but reserver is, or if the
	 * reservation has been made and confirmed (price is not null).
	 *
	 * @param reservationTimeout when the reservation will expire if not paid for.
	 * @throws IllegalStateException if timeout is not null but reserver is, or if
	 *                               new timeout and existing price are both not
	 *                               null
	 */
	public void setReservationTimeout(final LocalDateTime reservationTimeout) {
		if (reserver == null && reservationTimeout != null) {
			throw new IllegalStateException("Tickets only expire if someone reserved them");
		} else if (reserver != null && price != null && reservationTimeout != null) {
			throw new IllegalStateException("Only unconfirmed bookings can time out");
		}
		this.reservationTimeout = reservationTimeout;
	}

	/**
	 * This method throws if the booking ID is not null but the reserver is, or vice
	 * versa.
	 * @param bookingId the new booking ID.
	 * @throws IllegalStateException if it is null when reserver is not or it is not null when reserver is
	 */
	public void setBookingId(final String bookingId) {
		if (reserver == null && bookingId != null) {
			throw new IllegalStateException("Unbooked seat cannot have a booking ID");
		} else if (reserver != null && bookingId == null) {
			throw new IllegalStateException("Booked seat must have booking ID");
		}
		this.bookingId = bookingId;
	}

	/**
	 * @return whether this object's state is internally consistent.
	 */
	public boolean isValid() {
		if (reserver == null) {
			return reservationTimeout == null && price == null && bookingId == null;
		} else if (bookingId == null) {
			return false;
		} else if (price == null) {
			return reservationTimeout != null;
		} else {
			return reservationTimeout == null;
		}
	}
	/**
	 * @return whether the ticket has been booked
	 */
	@JsonGetter(value = "reserved")
	public boolean isReserved() {
		return reserver != null;
	}
}
