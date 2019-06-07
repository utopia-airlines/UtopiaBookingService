package com.sst.utopia.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sst.utopia.booking.model.User;

/**
 * A Data Access Object interface to access the table of users.
 *
 * @author Jonathan Lovelace
 */
@Repository
public interface UserDao extends JpaRepository<User, Integer> {}
