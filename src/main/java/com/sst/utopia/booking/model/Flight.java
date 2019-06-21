package com.sst.utopia.booking.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * A flight the airline is offering.
 *
 * While I don't expect instances of this class to be modified by user code
 * (since an admin interface is out of the scope of the Utopia Airlines project,
 * let alone the Booking service), and so have not provided setters, fields are
 * left non-final for the JPA.
 *
 * This class is Serializable because SeatLocation has to be and contains a
 * reference to a Flight object.
 *
 * @author Jonathan Lovelace
 */
@Entity
@Table(name = "tbl_flights")
public class Flight implements Serializable {
	/**
	 * Serialization version. Increment on any change to class structure that is (to
	 * be) pushed to production.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The flight's ID number in the database.
	 */
	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	/**
	 * The airport from which the flight departs.
	 */
	@ManyToOne
	@JoinColumn(name="departure")
	private Airport departureAirport;
	/**
	 * The date and time the flight is scheduled to depart.
	 */
	@Column
	private LocalDateTime departureDate;
	/**
	 * The airport to which the flight will arrive.
	 */
	@ManyToOne
	@JoinColumn(name="destination")
	private Airport destination;
	/**
	 * The date and time the flight is scheduled to arrive.
	 */
	@Column
	private LocalDateTime arrivalDate;

	/**
	 * The flight number that users will see. This is intended to be a hash derived
	 * from the ID number and the departure airport (and perhaps the destination
	 * airport), stored to speed the performance of queries filtering on it.
	 */
	@Column
	private int flightNumber;

	/**
	 * Default constructor, for JPA.
	 */
	public Flight() {
	}

	/**
	 * Full constructor, for tests.
	 */
	public Flight(final int id, final Airport departureAirport,
			final LocalDateTime departureDate, final Airport destination,
			final LocalDateTime arrivalDate, final int flightNumber) {
		this.id = id;
		this.departureAirport = departureAirport;
		this.departureDate = departureDate;
		this.destination = destination;
		this.arrivalDate = arrivalDate;
		this.flightNumber = flightNumber;
	}

	/**
	 * @return the internal ID number of the flight
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the airport from which the flight will depart
	 */
	public Airport getDepartureAirport() {
		return departureAirport;
	}

	/**
	 * @return when the flight is planned to depart
	 */
	public LocalDateTime getDepartureDate() {
		return departureDate;
	}

	/**
	 * @return the airport at which the flight will arrive
	 */
	public Airport getDestination() {
		return destination;
	}

	/**
	 * @return when the flight is planned to arrive
	 */
	public LocalDateTime getArrivalDate() {
		return arrivalDate;
	}

	/**
	 * @return the consumer-visible "flight number"
	 */
	public int getFlightNumber() {
		return flightNumber;
	}

	/**
	 * We use the primary-key ID number as our hash value.
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * An object is equal iff it is a Flight with the same ID and equal arrival and
	 * departure.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof Flight) {
			return id == ((Flight) obj).getId()
					&& Objects.equals(departureAirport, ((Flight) obj).getDepartureAirport())
					&& Objects.equals(departureDate, ((Flight) obj).getDepartureDate())
					&& Objects.equals(destination, ((Flight) obj).getDestination())
					&& Objects.equals(arrivalDate, ((Flight) obj).getArrivalDate());
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return String.format(
				"Flight %d (internal #%d) from %s (%s) at %s %s to %s (%s) at %s %s",
				flightNumber, id, departureAirport.getCode(),
				departureAirport.getName(), departureDate.toLocalDate().toString(),
				departureDate.toLocalTime().toString(), destination.getCode(),
				destination.getName(), arrivalDate.toLocalDate().toString(),
				arrivalDate.toLocalTime().toString());
	}
}
