package org.swa.conf.datatypes;

class CloneableUser extends User {

	private static final long serialVersionUID = 1L;

	@Override
	public User clone() {
		final User o = new User();
		o.setId(getId());
		o.setLastPasswordChange(getLastPasswordChange());
		o.setName(getName());
		o.setPassword(getPassword());
		o.setEmail(getEmail());
		return o;
	}
}