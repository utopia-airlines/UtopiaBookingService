package com.sst.utopia.booking.service;

import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import com.sst.utopia.booking.dao.TicketDao;
import com.sst.utopia.booking.model.SeatLocation;
import com.sst.utopia.booking.model.Ticket;
import com.sst.utopia.booking.model.User;

/**
 * Main business-logic class.
 *
 * @author Jonathan Lovelace
 */
public final class BookingService {
	/**
	 * DAO to access ticket table.
	 */
	@Autowired
	private TicketDao ticketDao;

	/**
	 * Default unpaid-booking expiration, in minutes.
	 */
	@Value("${utopia.expiration.minutes}")
	private int defaultBookingExpiration;

	/**
	 * Logger for handling errors in the DAO layer.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(BookingService.class.getName());

	/**
	 * The currently-active transaction, or null if not in a transaction.
	 */
	private volatile TransactionStatus transaction;
	/**
	 * The transaction manager provided by Spring.
	 */
	@Autowired
	private PlatformTransactionManager transactionManager;

	/**
	 * Begin a SQL transaction.
	 *
	 * @throws TransactionException on error in the ORM or transaction manager.
	 */
	private void beginTransaction() throws TransactionException {
		if (transaction == null) {
			synchronized (this) {
				if (transaction == null) {
					transaction = transactionManager.getTransaction(null);
				}
			}
		}
	}

	/**
	 * Commit the current transaction, if any.
	 */
	private void commit() {
		synchronized (this) {
			if (transaction != null) {
				transactionManager.commit(transaction);
				transaction = null;
			}
		}
	}

	/**
	 * Roll back the current transaction.
	 *
	 * @param <E>     the type of the exception that caused the rollback.
	 * @param pending the exception that caused us to roll the transaction back
	 * @return that exception, with a 'rollback failed' exception added as
	 *         suppressed if the rollback failed.
	 */
	private <E extends Exception> E rollback(final E pending) {
		try {
			if (transaction != null) {
				transactionManager.rollback(transaction);
			}
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "Further error while rolling back transaction",
					except);
			pending.addSuppressed(except);
		}
		synchronized (this) {
			transaction = null;
		}
		return pending;
	}

	/**
	 * Book a ticket for the specified seat (on the specified flight) for the
	 * specified user.
	 *
	 * @param seat the seat to book
	 * @param user who to book the seat for
	 * @return the booking details
	 * @throws IllegalArgumentException if the seat is already booked (TODO: use a
	 *                                  custom exception)
	 */
	public Ticket bookTicket(final SeatLocation seat, final User user) {
		return bookTicket(seat, user,
				LocalDateTime.now().plusMinutes(defaultBookingExpiration));
	}

	/**
	 * Book a ticket for the specified seat (on the specified flight) for the
	 * specified user, with the booking timing out if not paid before the specified
	 * time. This method is not intended to be called by any code outside this class
	 * other than test code.
	 *
	 * @param seat    the seat to book
	 * @param user    who to book the seat for
	 * @param timeout when the reservation should expire if not confirmed (paid)
	 * @return the booking details
	 * @throws IllegalArgumentException if the seat is already booked (TODO: use a
	 *                                  custom exception)
	 */
	public Ticket bookTicket(final SeatLocation seat, final User user,
			final LocalDateTime timeout) {
		final Ticket ticket = ticketDao.getOne(seat);
		if (ticket.getReserver() != null) {
			throw new IllegalArgumentException("Ticket already reserved");
		}
		beginTransaction();
		try {
			ticket.setReserver(user);
			ticket.setReservationTimeout(timeout);
			ticketDao.saveAndFlush(ticket);
		} catch (final RuntimeException exception) {
			throw rollback(exception);
		}
		commit();
		return ticket;
	}

	/**
	 * Mark the given ticket as having been paid for at the specified price. TODO:
	 * Should we only allow the ticket-holder to pay for it?
	 *
	 * @param ticket the ticket in question
	 * @param price  the price the ticket-holder paid
	 * @return the updated booking information
	 * @throws IllegalArgumentException if ticket is not booked or has already been
	 *                                  paid for
	 */
	public Ticket acceptPayment(final Ticket ticket, final int price) {
		final Ticket booking = ticketDao.getOne(ticket.getId());
		if (booking.getReserver() == null) {
			throw new IllegalArgumentException("Ticket is not booked");
		} else if (booking.getPrice() != null) {
			throw new IllegalArgumentException("Ticket has already been paid for");
		}
		beginTransaction();
		try {
			booking.setPrice(price);
			ticketDao.saveAndFlush(booking);
		} catch (final RuntimeException exception) {
			throw rollback(exception);
		}
		commit();
		return booking;
	}

	/**
	 * Cancel a reservation that has been made but not paid for. We assume that this
	 * will usually be called by some background process (cron job equivalent) to
	 * clean up unconfirmed bookings, but the frontend may call it either when the
	 * ticket times out or if the user explicitly cancels before paying.
	 *
	 * @param ticket the booking in question (only the ID fields are used)
	 * @throws IllegalArgumentException if the ticket has been paid for
	 */
	public void cancelPendingReservation(final Ticket ticket) {
		final Ticket booking = ticketDao.getOne(ticket.getId());
		if (booking.getReserver() == null) {
			return;
		} else if (booking.getPrice() != null) {
			throw new IllegalArgumentException("Ticket has been paid for");
		}
		beginTransaction();
		try {
			booking.setReserver(null);
			ticketDao.saveAndFlush(booking);
		} catch (final RuntimeException exception) {
			throw rollback(exception);
		}
		commit();
	}

}
