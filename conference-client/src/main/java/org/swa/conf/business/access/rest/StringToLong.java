package org.swa.conf.business.access.rest;

public class StringToLong {

	/** Decodes a hexadecimal (prefixed with x or X) or signed decimal string to Long, like Long.decode() */
	public static Long decode(final String s) throws NumberFormatException {

		if (s == null || s.isEmpty())
			return null;

		final char c = s.charAt(0);
		if (c == 'x' || c == 'X') {
			return s.length() == 1 ? null : Long.valueOf(s.substring(1), 16);
		}

		return Long.valueOf(s);
	}
}