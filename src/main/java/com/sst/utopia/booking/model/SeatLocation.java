package com.sst.utopia.booking.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Composite key for the Ticket class, consisting of the flight (ID, not
 * customer-visible "flight number"), row, and seat.
 *
 * Declared to implement Serializable because "Composite-id class must implement
 * Serializable."
 *
 * @author Jonathan Lovelace
 */
@Embeddable
public class SeatLocation implements Serializable {
    /**
     * Serialization version. Increment on any change to class structure that is
     * (to be) pushed to production.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The flight this seat is on.
     */
    @ManyToOne
    @JoinColumn(name="flight")
    private Flight flight;

    /**
     * The row this seat is in.
     */
    @Column
    private int row;

    /**
     * Which seat this is in the row.
     */
    @Column
    private String seat;
    /**
     * Default constructor for JPA.
     */
    public SeatLocation() {
    }
    /**
     * Full constructor for tests.
     */
    public SeatLocation(final Flight flight, final int row, final String seat) {
        this.flight = flight;
        this.row = row;
        this.seat = seat;
    }

    /**
     * @return the flight this seat is on
     */
	public Flight getFlight() {
		return flight;
	}

	/**
	 * @return the row this seat is in
	 */
	public int getRow() {
		return row;
	}

	/**
	 * @return which seat this is in the row
	 */
	public String getSeat() {
		return seat;
	}

	@Override
	public int hashCode() {
		return Objects.hash(flight, row, seat);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof SeatLocation) {
			return Objects.equals(flight, ((SeatLocation) obj).getFlight())
					&& row == ((SeatLocation) obj).getRow()
					&& Objects.equals(seat, ((SeatLocation) obj).getSeat());
		} else {
			return false;
		}
	}
}
