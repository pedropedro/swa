package org.swa.conf.business.mock;

import org.swa.conf.datatypes.Conference;

public class ConferenceCollection extends Conference {

	private static final long	serialVersionUID	= 1L;

	public ConferenceCollection() {
	}

	public ConferenceCollection(final Conference c) {
		setCity(c.getCity());
		setDescription(c.getDescription());
		setFrom(c.getFrom());
		setId(c.getId());
		setName(c.getName());
		setTalks(c.getTalks());
		setTo(c.getTo());
	}

	@Override
	public Long getId() {
		return (Long) super.getId();
	}
}