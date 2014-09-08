package org.swa.conf.datatypes;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.swa.conf.datatypes.validators.Email;

public class Speaker extends AbstractDatatype {

	private static final long serialVersionUID = 1L;

	private String name;
	private String description;
	private String email;
	private User user;

	@NotNull
	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	@Email
	public String getEmail() {
		return email;
	}

	@Valid
	public User getUser() {
		return user;
	}

	public Speaker setName(final String name) {
		this.name = name;
		return this;
	}

	public Speaker setDescription(final String description) {
		this.description = description;
		return this;
	}

	public Speaker setEmail(final String email) {
		this.email = email;
		return this;
	}

	public Speaker setUser(final User user) {
		this.user = user;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		// Intentionally NOT getClass() comparison
		if (!(obj instanceof Speaker))
			return false;
		final Speaker other = (Speaker) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Speaker [id=");
		builder.append(getId());
		builder.append(", name=");
		builder.append(name);
		builder.append(", description=");
		builder.append(description);
		builder.append(", email=");
		builder.append(email);
		builder.append(", user=");
		builder.append(user);
		builder.append("]");
		return builder.toString();
	}
}