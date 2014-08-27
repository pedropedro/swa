package org.swa.conf.datatypes;

import java.io.Serializable;

public abstract class AbstractDatatype implements Serializable, Cloneable {

	private static final long	serialVersionUID	= 1L;

	private long _id;

	public long getId() {
		return _id;
	}

	public AbstractDatatype setId(final long id) {
		_id = id;
		return this;
	}

	@Override
	public int hashCode() {
		return (int) _id;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		// Intentionally NOT getClass() comparison
		if (!(obj instanceof AbstractDatatype))
			return false;
		final AbstractDatatype other = (AbstractDatatype) obj;

		return _id == other._id;
	}
}