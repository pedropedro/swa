package org.swa.conf.datatypes;

import java.util.Date;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;

import org.swa.conf.datatypes.validators.Email;

public class User extends AbstractDatatype {

	private static final long serialVersionUID = 1L;

	private String name;
	private String password;
	private Date lastPasswordChange;
	private String email;

	@NotNull
	public String getName() {
		return name;
	}

	@Size(min = 8, max = 128 /* let give NSA a chance ! */)
	public String getPassword() {
		return password;
	}

	@Past
	public Date getLastPasswordChange() {
		return lastPasswordChange;
	}

	@Email
	@NotNull
	public String getEmail() {
		return email;
	}

	public User setName(final String name) {
		this.name = name;
		return this;
	}

	public User setPassword(final String password) {
		this.password = password;
		return this;
	}

	public User setLastPasswordChange(final Date lastPasswordChange) {
		this.lastPasswordChange = lastPasswordChange;
		return this;
	}

	public User setEmail(final String email) {
		this.email = email;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((lastPasswordChange == null) ? 0 : lastPasswordChange.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		// Intentionally NOT getClass() comparison
		if (!(obj instanceof User))
			return false;
		final User other = (User) obj;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (lastPasswordChange == null) {
			if (other.lastPasswordChange != null)
				return false;
		} else if (!lastPasswordChange.equals(other.lastPasswordChange))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("User [id=");
		builder.append(getId());
		builder.append(", name=");
		builder.append(name);
		builder.append(", email=");
		builder.append(email);
		builder.append(", password=");
		builder.append(password);
		builder.append(", lastPasswordChange=");
		builder.append(lastPasswordChange);
		builder.append("]");
		return builder.toString();
	}
}