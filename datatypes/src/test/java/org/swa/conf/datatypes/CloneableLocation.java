package org.swa.conf.datatypes;

class CloneableLocation extends Location {

	private static final long serialVersionUID = 1L;

	@Override
	public Location clone() {
		final Location o = new Location();
		o.setId(getId());
		o.setName(getName());
		o.setLatitude(getLatitude());
		o.setLongitude(getLongitude());
		o.setRooms(getRooms());
		o.setAddress(getAddress());

		return o;
	}
}