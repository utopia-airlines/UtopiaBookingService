package com.sst.utopia.booking.model;

/**
 * A simple wrapper around a single integer representing an amount paid for a
 * ticket.
 *
 * @author Jonathan Lovelace
 */
public class PaymentAmount {
	/**
	 * The amount paid.
	 */
	private int price;
	/**
	 * @return the amount paid
	 */
	public int getPrice() {
		return price;
	}
}
