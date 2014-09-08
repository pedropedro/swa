package org.swa.conf.datatypes;

import java.io.Serializable;

public abstract class AbstractDatatype implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	private Long _id;

	public Long getId() {
		return _id;
	}

	public AbstractDatatype setId(final Long id) {
		_id = id;
		return this;
	}

	@Override
	public int hashCode() {
		return _id == null ? 31 : _id.hashCode();
	}

	@Override
	public boolean equals(final Object o) {

		if (this == o) return true;

		// Intentionally NOT getClass() comparison
		if (!(o instanceof AbstractDatatype)) return false;

		final AbstractDatatype that = (AbstractDatatype) o;

		if (_id != null ? !_id.equals(that._id) : that._id != null) return false;

		return true;
	}
}