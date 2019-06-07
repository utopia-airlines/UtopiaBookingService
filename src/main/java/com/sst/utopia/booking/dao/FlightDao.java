package com.sst.utopia.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sst.utopia.booking.model.Flight;

/**
 * A Data Access Object interface to access the table of flights.
 *
 * @author Jonathan Lovelace
 */
@Repository
public interface FlightDao extends JpaRepository<Flight, Integer> {}
