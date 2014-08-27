package org.swa.conf.business.events;

import org.swa.conf.datatypes.User;

public class PasswordExpiredEvent {

	private final User	user;

	public PasswordExpiredEvent(final User user) {
		if (user == null)
			throw new IllegalArgumentException("User is mandatory");

		this.user = new User();
		this.user.setEmail(user.getEmail());
		this.user.setId(user.getId());
		this.user.setLastPasswordChange(user.getLastPasswordChange());
		this.user.setName(user.getName());
		this.user.setPassword(null); // Datenschutz
	}

	public User getUser() {
		return user;
	}
}