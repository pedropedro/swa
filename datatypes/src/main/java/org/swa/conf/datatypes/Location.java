package org.swa.conf.datatypes;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;

import org.swa.conf.datatypes.validators.GeoLatitude;
import org.swa.conf.datatypes.validators.GeoLongitude;

public class Location extends AbstractDatatype {

	private static final long serialVersionUID = 1L;

	private String city;
	private String street;
	private Double latitude;
	private Double longitude;
	private List<? extends Room> rooms;

	@NotNull
	public String getCity() {
		return city;
	}

	@NotNull
	public String getStreet() {
		return street;
	}

	@GeoLatitude
	public Double getLatitude() {
		return latitude;
	}

	@GeoLongitude
	@/* just for test'n'fun */NotNull
	public Double getLongitude() {
		return longitude;
	}

	public List<? extends Room> getRooms() {
		if (rooms == null)
			rooms = new ArrayList<>();

		return rooms;
	}

	public Location setCity(final String city) {
		this.city = city;
		return this;
	}

	public Location setStreet(final String street) {
		this.street = street;
		return this;
	}

	public Location setLatitude(final Double latitude) {
		this.latitude = latitude;
		return this;
	}

	public Location setLongitude(final Double longitude) {
		this.longitude = longitude;
		return this;
	}

	public Location setRooms(final List<? extends Room> rooms) {
		this.rooms = rooms;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((latitude == null) ? 0 : latitude.hashCode());
		result = prime * result + ((longitude == null) ? 0 : longitude.hashCode());
		result = prime * result + ((rooms == null) ? 0 : rooms.hashCode());
		result = prime * result + ((street == null) ? 0 : street.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		// Intentionally NOT getClass() comparison
		if (!(obj instanceof Location))
			return false;
		final Location other = (Location) obj;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (latitude == null) {
			if (other.latitude != null)
				return false;
		} else if (!latitude.equals(other.latitude))
			return false;
		if (longitude == null) {
			if (other.longitude != null)
				return false;
		} else if (!longitude.equals(other.longitude))
			return false;
		if (rooms == null) {
			if (other.rooms != null)
				return false;
		} else if (!rooms.equals(other.rooms))
			return false;
		if (street == null) {
			if (other.street != null)
				return false;
		} else if (!street.equals(other.street))
			return false;
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Location [id=");
		builder.append(getId());
		builder.append(", city=");
		builder.append(city);
		builder.append(", street=");
		builder.append(street);
		builder.append(", latitude=");
		builder.append(latitude);
		builder.append(", longitude=");
		builder.append(longitude);
		builder.append(", rooms=");
		builder.append(rooms);
		builder.append("]");
		return builder.toString();
	}
}