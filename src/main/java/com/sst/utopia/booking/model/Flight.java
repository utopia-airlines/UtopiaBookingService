package com.sst.utopia.booking.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
	 * The airport and date and time of departure.
	 */
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "airport", column = @Column(name = "departure")),
			@AttributeOverride(name = "date", column = @Column(name = "departure_date")) })
	private AirportDateDTO departure;

	/**
	 * The airport and date and time of arrival.
	 */
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "airport", column = @Column(name = "destination")),
			@AttributeOverride(name = "date", column = @Column(name = "arrival_date")) })
	private AirportDateDTO arrival;

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
	public Flight() {}

	/**
	 * Full constructor, for tests.
	 */
	public Flight(final int id, final AirportDateDTO departure, final AirportDateDTO arrival, final int flightNumber) {
		this.id = id;
		this.departure = departure;
		this.arrival = arrival;
		this.flightNumber = flightNumber;
	}

	/**
	 * @return the internal ID number of the flight
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the location and planned date and time of departure
	 */
	public AirportDateDTO getDeparture() {
		return departure;
	}

	/**
	 * @return the location and planned date and time of arrival
	 */
	public AirportDateDTO getArrival() {
		return arrival;
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
					&& Objects.equals(departure, ((Flight) obj).getDeparture())
					&& Objects.equals(arrival, ((Flight) obj).getArrival());
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return String.format("Flight %d (internal #%d) from %s to %s", flightNumber,
				id, departure.toString(), arrival.toString());
	}
}
