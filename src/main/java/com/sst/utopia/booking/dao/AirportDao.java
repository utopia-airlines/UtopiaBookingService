package com.sst.utopia.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sst.utopia.booking.model.Airport;

/**
 * A Data Access Object interface to access the table of airports.
 *
 * @author Jonathan Lovelace
 */
@Repository
public interface AirportDao extends JpaRepository<Airport, String> {}
