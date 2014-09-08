package org.swa.conf.datatypes;

import javax.inject.Inject;
import javax.validation.ValidationException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.swa.conf.datatypes.validators.ModelValidator;

@RunWith(Arquillian.class)
public class RoomExternalOverriddenTest {

	@Deployment
	public static Archive<?> createTestArchive() {
		final WebArchive war = ArchiveProducer.createTestArchive();
		war.addAsResource("validation.xml", "META-INF/validation.xml");
		war.addAsResource("room-constraints.xml", "META-INF/validation/room-constraints.xml");
		System.out.println(war.toString(true));
		return war;
	}

	@Inject
	private ModelValidator validator;

	@Test
	public void rangeMinMaxOverriddenByXmlConfigTest() {

		final Room r = new Room();
		r.setId(1l);
		r.setName("name");

		r.setCapacity(0);
		validator.validate(r);

		r.setCapacity(42);
		validator.validate(r);

		try {
			validator.validate(r.setCapacity(-1));
			fail("Expected validation exception");
		} catch (final ValidationException e) {
			assertTrue(e.getMessage(), e.getMessage().endsWith("\n" + "room.capacity: value must be within range 0 " +
					"to 42"));
		}
		try {
			validator.validate(r.setCapacity(43));
			fail("Expected validation exception");
		} catch (final ValidationException e) {
			assertTrue(e.getMessage(), e.getMessage().endsWith("\n" + "room.capacity: value must be within range 0 " +
					"to 42"));
		}
	}
}