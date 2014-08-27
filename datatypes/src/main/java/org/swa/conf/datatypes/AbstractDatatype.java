package org.swa.conf.datatypes;

import java.io.Serializable;

public abstract class AbstractDatatype implements Serializable, Cloneable {

	private static final long	serialVersionUID	= 1L;

	private Object						_id;

	public Object getId() {
		return _id;
	}

	public AbstractDatatype setId(final Object id) {
		_id = id;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		return result;
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
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		return true;
	}
}