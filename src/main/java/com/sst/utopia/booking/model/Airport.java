package com.sst.utopia.booking.model;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * An airport that flights can fly to or from.
 *
 * Objects of this class should in general be immutable, so no setters are
 * provided, but to support JPA providers fields are not made final.
 *
 * @author Jonathan Lovelace
 */
@Entity
@Table(name = "tbl_airports")
public class Airport {
	/**
	 * The code identifying the airport.
	 */
	@Id
	@Column
	private String code;
	/**
	 * The name of the airport.
	 */
	@Column
	private String name;

	/**
	 * @return the code identifying the airport
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return the name of the airport
	 */
	public String getName() {
		return name;
	}

	/**
	 * An object is equal to this one iff it is an Airport with the same code.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof Airport) {
			return Objects.equals(code, ((Airport) obj).getCode());
		} else {
			return false;
		}
	}

	/**
	 * We use the code's hash code as ours.
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(code);
	}

	@Override
	public String toString() {
		return String.format("%s: %s", code, name);
	}
}
