package org.swa.conf.datatypes;

class CloneableSpeaker extends Speaker {

	private static final long serialVersionUID = 1L;

	@Override
	public Speaker clone() {
		final Speaker o = new Speaker();
		o.setId(getId());
		o.setName(getName());
		o.setEmail(getEmail());
		o.setDescription(getDescription());
		o.setUser(getUser() == null ? null : ((CloneableUser) getUser()).clone());
		return o;
	}
}