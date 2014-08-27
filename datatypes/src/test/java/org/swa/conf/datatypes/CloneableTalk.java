package org.swa.conf.datatypes;

class CloneableTalk extends Talk {

	private static final long	serialVersionUID	= 1L;

	@Override
	public Talk clone() {
		final Talk o = new Talk();
		o.setId(getId());
		o.setName(getName());
		o.setFrom(getFrom());
		o.setRooms(getRooms());
		o.setShortAbstract(getShortAbstract());
		o.setSpeakers(getSpeakers());
		o.setTo(getTo());

		return o;
	}
}