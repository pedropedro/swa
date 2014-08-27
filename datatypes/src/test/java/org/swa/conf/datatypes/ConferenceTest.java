package org.swa.conf.datatypes;

import static org.junit.Assert.*;

import java.util.Date;

import javax.inject.Inject;
import javax.validation.ValidationException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.swa.conf.datatypes.validators.ModelValidator;

@RunWith(Arquillian.class)
public class ConferenceTest {

	@Deployment
	public static Archive<?> createTestArchive() {
		return ArchiveProducer.createTestArchive();
	}

	@Inject
	private ModelValidator	validator;

	@Test
	public void crossAttributesTest() {

		final Conference c = new Conference();
		c.setId(1l);
		c.setName("name");

		validator.validate(c);

		c.setFrom(new Date(0));
		validator.validate(c);

		c.setTo(new Date(0));
		validator.validate(c);

		try {
			validator.validate(c.setFrom(new Date(1)));
			fail("Expected validation exception");
		} catch (final ValidationException e) {
			assertTrue(e.getMessage(), e.getMessage().endsWith(
					"\n" + "conference.timeIntervalValid: start time point must precede the end"));
		}
	}
}