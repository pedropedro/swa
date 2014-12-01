package org.swa.conf.app.web.security;

import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * Implements <a href="http://tools.ietf.org/html/rfc6750">RFC 6750 Bearer Token</a>
 */
public class BearerToken {

	private static final int TOKEN_SIZE = 32;

	private static final SecureRandom SR = new SecureRandom();

	/** sub-range of allowed characters for a bearer token (array length 64 bytes) */
	private static final char[] C = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789._".toCharArray();

	private static final Pattern P = Pattern.compile("[A-Za-z0-9\\-\\._~\\+/]{" + TOKEN_SIZE + "}");

	public static String generate() {
		final byte[] bytes = new byte[TOKEN_SIZE];
		SR.nextBytes(bytes);
		final StringBuilder sb = new StringBuilder(TOKEN_SIZE);
		for (final byte b : bytes) sb.append(C[b & 0x3F]);
		return sb.toString();
	}

	public static void assertValidity(final String token) throws IllegalArgumentException {
		if (token == null || token.isEmpty()) throw new IllegalArgumentException("No token provided");
		if (!P.matcher(token).matches()) throw new IllegalArgumentException("Invalid token: " + token);
	}

	public static void main(String[] params){
		for (int i = 0; i < 1000; i++) {
			final String s = generate();
			System.out.println(s);
			assertValidity(s);
		}
	}
}