package com.sst.utopia.booking.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sst.utopia.booking.model.Flight;

/**
 * A Data Access Object interface to access the table of flights.
 *
 * @author Jonathan Lovelace
 */
@Repository
public interface FlightDao extends JpaRepository<Flight, Integer> {
	/**
	 * Get the flight, if any, with the given flight number.
	 * @param flightNumber the flight number
	 * @return the flight with that number, if any
	 */
	List<Flight> findByFlightNumber(int flightNumber);
}
