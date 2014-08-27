package org.swa.conf.datatypes;

import javax.validation.constraints.NotNull;

import org.swa.conf.datatypes.validators.Range;

public class Room extends AbstractDatatype {

	private static final long	serialVersionUID	= 1L;

	private String						name;
	private Integer						capacity;

	@NotNull
	public String getName() {
		return name;
	}

	@Range(min = "5", max = "1000")
	@NotNull
	public Integer getCapacity() {
		return capacity;
	}

	public Room setName(final String name) {
		this.name = name;
		return this;
	}

	public Room setCapacity(final Integer capacity) {
		this.capacity = capacity;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((capacity == null) ? 0 : capacity.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		// Intentionally NOT getClass() comparison
		if (!(obj instanceof Room))
			return false;
		final Room other = (Room) obj;
		if (capacity == null) {
			if (other.capacity != null)
				return false;
		} else if (!capacity.equals(other.capacity))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Room [id=");
		builder.append(getId());
		builder.append(", name=");
		builder.append(name);
		builder.append(", capacity=");
		builder.append(capacity);
		builder.append("]");
		return builder.toString();
	}
}