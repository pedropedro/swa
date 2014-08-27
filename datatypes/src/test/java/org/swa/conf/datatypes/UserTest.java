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
public class UserTest {

	@Deployment
	public static Archive<?> createTestArchive() {
		return ArchiveProducer.createTestArchive();
	}

	@Inject
	private ModelValidator	validator;

	@Test
	public void validationTest() {

		final CloneableUser u = new CloneableUser();
		u.setId(1l);
		u.setLastPasswordChange(null);
		u.setName("name");
		u.setPassword(null);
		u.setEmail("a@bc.de");

		validator.validate(u);

		try {
			validator.validate(u.clone().setName(null));
			fail("Expected validation exception");
		} catch (final ValidationException e) {
			assertTrue(e.getMessage(), e.getMessage().endsWith("\n" + "user.name: may not be null"));
		}
		try {
			validator.validate(u.clone().setName(null).setId(null));
			fail("Expected validation exception");
		} catch (final ValidationException e) {
			assertFalse(e.getMessage(), e.getMessage().contains("\n" + "user.id: may not be null"));
			assertTrue(e.getMessage(), e.getMessage().contains("\n" + "user.name: may not be null"));
		}
		try {
			validator.validate(u.clone().setPassword("1234567"));
			fail("Expected validation exception");
		} catch (final ValidationException e) {
			assertTrue(e.getMessage(), e.getMessage().endsWith("\n" + "user.password: size must be between 8 and 128"));
			validator.validate(u.clone().setPassword("12345678"));
		}
		try {
			validator.validate(u.clone().setLastPasswordChange(new Date(new Date().getTime() + 10000l)));
			fail("Expected validation exception");
		} catch (final ValidationException e) {
			assertTrue(e.getMessage(), e.getMessage().endsWith("\n" + "user.lastPasswordChange: must be in the past"));
			validator.validate(u.clone().setLastPasswordChange(new Date(0)));
		}
		try {
			validator.validate(u.clone().setEmail(null));
			fail("Expected validation exception");
		} catch (final ValidationException e) {
			assertTrue(e.getMessage(), e.getMessage().endsWith("\n" + "user.email: may not be null"));
		}
		try {
			validator.validate(u.clone().setEmail("a"));
			fail("Expected validation exception");
		} catch (final ValidationException e) {
			assertTrue(e.getMessage(), e.getMessage().endsWith("\n" + "user.email: address not valid"));
		}
		try {
			validator.validate(u.clone().setEmail("a@"));
			fail("Expected validation exception");
		} catch (final ValidationException e) {
			assertTrue(e.getMessage(), e.getMessage().endsWith("\n" + "user.email: address not valid"));
		}
		try {
			validator.validate(u.clone().setEmail("a@bc"));
			fail("Expected validation exception");
		} catch (final ValidationException e) {
			assertTrue(e.getMessage(), e.getMessage().endsWith("\n" + "user.email: address not valid"));
		}
	}
}