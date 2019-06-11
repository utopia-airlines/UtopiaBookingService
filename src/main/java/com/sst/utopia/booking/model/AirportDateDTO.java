package com.sst.utopia.booking.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Embeddable;

/**
 * A particular airport at a particular date. This class only exists because
 * this combination occurs twice in a Flight object. It is Serializable because
 * Flight has to be and contains a reference to an object of this class.
 *
 * @author Jonathan Lovelace
 *
 */
@Embeddable
public class AirportDateDTO implements Serializable {
	/**
	 * Serialization version. Increment on any change to class structure that is (to
	 * be) pushed to production.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The airport that the flight is departing from or arriving at.
	 */
	private Airport airport;
	/**
	 * The date and time that the flight is expected to depart or arrive.
	 */
	private LocalDateTime date;
	/**
	 * Default constructor for JPA.
	 */
	public AirportDateDTO() {
	}
	/**
	 * Full constructor for tests.
	 */
	public AirportDateDTO(final Airport airport, final LocalDateTime date) {
		this.airport = airport;
		this.date = date;
	}
	/**
	 * @return the airport that the flight is departing from or arriving at.
	 */
	public Airport getAirport() {
		return airport;
	}

	/**
	 * @return the date and time that the flight is expected to depart or arrive.
	 */
	public LocalDateTime getDate() {
		return date;
	}

	/**
	 * An object is equal to this one iff it is the same airport and date (including
	 * time).
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof AirportDateDTO) {
			return Objects.equals(airport, ((AirportDateDTO) obj).getAirport())
					& Objects.equals(date, ((AirportDateDTO) obj).getDate());
		} else {
			return false;
		}
	}

	/**
	 * We calculate our hash code based on the two fields.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(airport, date);
	}

	/**
	 * @return a String representation of this object.
	 */
	@Override
	public String toString() {
		return String.format("%s (%s) at %s %s", airport.getCode(),
				airport.getName(), date.toLocalDate().toString(),
				date.toLocalTime().toString());
	}
}
