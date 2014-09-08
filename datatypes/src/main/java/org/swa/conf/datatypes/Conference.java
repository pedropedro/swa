package org.swa.conf.datatypes;

import java.util.Date;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

public class Conference extends AbstractDatatype {

	private static final long serialVersionUID = 1L;

	private String name;
	private String description;
	private Date from;
	private Date to;
	private Location city;
	private List<? extends Talk> talks;

	/** Cross-property validation - check the visibility !!! */
	@AssertTrue(message = "{invalid.time-interval}")
	private boolean isTimeIntervalValid() {
		return from == null || to == null || !from.after(to);
	}

	@NotNull
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Date getFrom() {
		return from;
	}

	public Date getTo() {
		return to;
	}

	@Valid
	public Location getCity() {
		return city;
	}

	public List<? extends Talk> getTalks() {
		return talks;
	}

	public Conference setName(final String name) {
		this.name = name;
		return this;
	}

	public Conference setDescription(final String description) {
		this.description = description;
		return this;
	}

	public Conference setFrom(final Date from) {
		this.from = from;
		return this;
	}

	public Conference setTo(final Date to) {
		this.to = to;
		return this;
	}

	public Conference setCity(final Location city) {
		this.city = city;
		return this;
	}

	public Conference setTalks(final List<? extends Talk> talks) {
		this.talks = talks;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((talks == null) ? 0 : talks.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		// Intentionally NOT getClass() comparison
		if (!(obj instanceof Conference))
			return false;
		final Conference other = (Conference) obj;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (talks == null) {
			if (other.talks != null)
				return false;
		} else if (!talks.equals(other.talks))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Conference [id=");
		builder.append(getId());
		builder.append(", name=");
		builder.append(name);
		builder.append(", description=");
		builder.append(description);
		builder.append(", from=");
		builder.append(from);
		builder.append(", to=");
		builder.append(to);
		builder.append(", city=");
		builder.append(city);
		builder.append(", talks=");
		builder.append(talks);
		builder.append("]");
		return builder.toString();
	}
}