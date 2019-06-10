package com.sst.utopia.booking.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sst.utopia.booking.model.SeatLocation;
import com.sst.utopia.booking.model.Ticket;

/**
 * A Data Access Object interface to access the table of tickets/seats.
 *
 * @author Jonathan Lovelace
 */
@Repository
public interface TicketDao extends JpaRepository<Ticket, SeatLocation> {
	/**
	 * Get the ticket, if any, with the given booking ID.
	 * @param bookingId the booking ID
	 * @return the ticket with that booking ID, if any
	 */
	List<Ticket> findByBookingId(String bookingId);
}
