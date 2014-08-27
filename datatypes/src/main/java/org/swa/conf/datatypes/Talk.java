package org.swa.conf.datatypes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

public class Talk extends AbstractDatatype {

	private static final long				serialVersionUID	= 1L;

	private String									name;
	private String									shortAbstract;
	private Date										from;
	private Date										to;
	private List<? extends Speaker>	speakers;
	private List<? extends Room>		rooms;

	/** Cross-property validation - check the visibility !!! */
	@AssertTrue(message = "{invalid.time-interval}")
	private boolean isTimeIntervalValid() {
		return from == null || to == null || !from.after(to);
	}

	@NotNull
	public String getName() {
		return name;
	}

	public String getShortAbstract() {
		return shortAbstract;
	}

	public Date getFrom() {
		return from;
	}

	public Date getTo() {
		return to;
	}

	public List<? extends Speaker> getSpeakers() {
		if (speakers == null)
			speakers = new ArrayList<>();

		return speakers;
	}

	public List<? extends Room> getRooms() {
		if (rooms == null)
			rooms = new ArrayList<>();

		return rooms;
	}

	public Talk setName(final String name) {
		this.name = name;
		return this;
	}

	public Talk setShortAbstract(final String shortAbstract) {
		this.shortAbstract = shortAbstract;
		return this;
	}

	public Talk setFrom(final Date from) {
		this.from = from;
		return this;
	}

	public Talk setTo(final Date to) {
		this.to = to;
		return this;
	}

	public Talk setSpeakers(final List<? extends Speaker> speakers) {
		this.speakers = speakers;
		return this;
	}

	public Talk setRooms(final List<? extends Room> rooms) {
		this.rooms = rooms;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((rooms == null) ? 0 : rooms.hashCode());
		result = prime * result + ((shortAbstract == null) ? 0 : shortAbstract.hashCode());
		result = prime * result + ((speakers == null) ? 0 : speakers.hashCode());
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
		if (!(obj instanceof Talk))
			return false;
		final Talk other = (Talk) obj;
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
		if (rooms == null) {
			if (other.rooms != null)
				return false;
		} else if (!rooms.equals(other.rooms))
			return false;
		if (shortAbstract == null) {
			if (other.shortAbstract != null)
				return false;
		} else if (!shortAbstract.equals(other.shortAbstract))
			return false;
		if (speakers == null) {
			if (other.speakers != null)
				return false;
		} else if (!speakers.equals(other.speakers))
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
		builder.append("Talk [id=");
		builder.append(getId());
		builder.append(", name=");
		builder.append(name);
		builder.append(", shortAbstract=");
		builder.append(shortAbstract);
		builder.append(", from=");
		builder.append(from);
		builder.append(", to=");
		builder.append(to);
		builder.append(", speakers=");
		builder.append(speakers);
		builder.append(", rooms=");
		builder.append(rooms);
		builder.append("]");
		return builder.toString();
	}
}