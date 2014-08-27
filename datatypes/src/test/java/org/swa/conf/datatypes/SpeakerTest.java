package org.swa.conf.datatypes;

import static org.junit.Assert.*;

import javax.inject.Inject;
import javax.validation.ValidationException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.swa.conf.datatypes.validators.ModelValidator;

@RunWith(Arquillian.class)
public class SpeakerTest {

	@Deployment
	public static Archive<?> createTestArchive() {
		return ArchiveProducer.createTestArchive();
	}

	@Inject
	private ModelValidator	validator;

	@Test
	public void objectTreeValidationTest() {

		final CloneableUser u = new CloneableUser();
		u.setId(1l);
		u.setLastPasswordChange(null);
		u.setName("name");
		u.setPassword("1");
		u.setEmail("a@bc.de");

		final CloneableSpeaker s = new CloneableSpeaker();
		s.setDescription(null);
		s.setEmail(null);
		s.setId(1L);
		s.setName("name");
		s.setUser(null);

		validator.validate(s);

		try {
			validator.validate(s.clone().setUser(u));
			fail("Expected validation exception");
		} catch (final ValidationException e) {
			// check the whole object tree
			assertTrue(e.getMessage(), e.getMessage()
					.endsWith("\n" + "speaker.user.password: size must be between 8 and 128"));
			validator.validate(u.clone().setPassword("12345678"));
		}
	}
}