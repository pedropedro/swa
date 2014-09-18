package org.swa.conf.mongo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

public class Utils {

	private static final SimpleDateFormat df = new SimpleDateFormat();

	static {
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		df.applyPattern("yyyy-MM-dd");
	}


	private static final SimpleDateFormat dtf = new SimpleDateFormat();

	static {
		dtf.setTimeZone(TimeZone.getTimeZone("GMT"));
		dtf.applyPattern("yyyy-MM-dd HH:mm");
	}


	/** yyyy-MM-dd */
	public static Date parseDate(final String dateString) {
		try {
			return df.parse(dateString);
		} catch (final ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/** yyyy-MM-dd HH:mm */
	public static Date parseDateTime(final String dateTimeString) {
		try {
			return dtf.parse(dateTimeString);
		} catch (final ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Test
	public void test() {
		System.out.println(parseDate("2000-06-01"));
		System.out.println(parseDateTime("2000-06-01 12:34"));
	}
}