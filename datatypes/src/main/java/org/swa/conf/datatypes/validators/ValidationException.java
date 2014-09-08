package org.swa.conf.datatypes.validators;

public class ValidationException extends javax.validation.ValidationException {

	private static final long serialVersionUID = 1L;

	public ValidationException(final String message) {
		super(message);
	}

	public String getMessageJson() {
		return "{\"!\":\"" + getMessage().replace("\n", "\\n") + "\"}";
	}

	public String getLocalizedMessageJson() {
		return "{\"!\":\"" + getLocalizedMessage().replace("\n", "\\n") + "\"}";
	}
}