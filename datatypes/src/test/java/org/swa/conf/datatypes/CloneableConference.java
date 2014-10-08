package org.swa.conf.datatypes;

class CloneableConference extends Conference {

	private static final long serialVersionUID = 1L;

	@Override
	public Conference clone() {
		final Conference o = new Conference();
		o.setId(getId());
		o.setLocation(getLocation());
		o.setDescription(getDescription());
		o.setFrom(getFrom());
		o.setName(getName());
		o.setTalks(getTalks());
		o.setTo(getTo());

		return o;
	}
}