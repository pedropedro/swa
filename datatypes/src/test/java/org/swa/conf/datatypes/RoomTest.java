package org.swa.conf.datatypes;

import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import javax.validation.ValidationException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.swa.conf.datatypes.validators.ModelValidator;

@RunWith(Arquillian.class)
public class RoomTest {

	@Deployment
	public static Archive<?> createTestArchive() {
		return ArchiveProducer.createTestArchive();
	}

	@Inject
	private ModelValidator validator;

	@Test
	public void rangeTest() {

		final CloneableRoom r = new CloneableRoom();
		r.setId(1l);
		r.setName("name");
		r.setCapacity(5);
		r.setDbl(1);

		validator.validate(r);

		try {
			validator.validate(r.clone().setCapacity(4));
			fail("Expected validation exception");
		} catch (final ValidationException e) {
			assertTrue(e.getMessage(), e.getMessage().endsWith("\n" + "room.capacity: value must be within range 5 " +
					"to 1000"));
			validator.validate(r.clone().setCapacity(1000));
		}
		try {
			validator.validate(r.clone().setCapacity(1001));
			fail("Expected validation exception");
		} catch (final ValidationException e) {
			assertTrue(e.getMessage(), e.getMessage().endsWith("\n" + "room.capacity: value must be within range 5 " +
					"to 1000"));
		}
		try {
			validator.validate(r.clone().setCapacity(null));
			fail("Expected validation exception");
		} catch (final ValidationException e) {
			assertTrue(e.getMessage(), e.getMessage().endsWith("\n" + "room.capacity: may not be null"));
		}
	}

	@Test
	public void rangeDoubleTest() {

		final CloneableRoom r = new CloneableRoom();
		r.setId(1l);
		r.setName("name");
		r.setCapacity(5);

		// min() -------------
		r.setDbl(1f);
		validator.validate(r);

		r.setDbl(1d);
		validator.validate(r);

		r.setDbl(1.0);
		validator.validate(r);

		r.setDbl(1);
		validator.validate(r);

		r.setDbl(1l);
		validator.validate(r);

		// max() -------------
		r.setDbl(10f);
		validator.validate(r);

		r.setDbl(10d);
		validator.validate(r);

		r.setDbl(10.0);
		validator.validate(r);

		r.setDbl(10);
		validator.validate(r);

		r.setDbl(10l);
		validator.validate(r);

		try {
			validator.validate(r.setDbl(0.99999999999999994));
			fail("Expected validation exception");
		} catch (final ValidationException e) {
			assertTrue(e.getMessage(), e.getMessage().endsWith(
					"\n" + "cloneableroom.dbl: value of the member dbl must be within range 1 to 10.0"));
		}
		try {
			validator.validate(r.setDbl(10.000000000000001));
			fail("Expected validation exception");
		} catch (final ValidationException e) {
			assertTrue(e.getMessage(), e.getMessage().endsWith(
					"\n" + "cloneableroom.dbl: value of the member dbl must be within range 1 to 10.0"));
		}
	}

	@Test
	public void rangeAtomicIntegerTest() {

		final CloneableRoom r = new CloneableRoom();
		r.setId(1l);
		r.setName("name");
		r.setCapacity(5);
		r.setDbl(1);
		r.setAi(null);
		validator.validate(r);

		try {
			validator.validate(r.setAi(new AtomicInteger(5)));
			fail("Expected validation exception");
		} catch (final ValidationException e) {
			assertTrue(e.getMessage(), e.getMessage().endsWith(
					"\n" + "cloneableroom.ai: Unexpected target class java.util.concurrent.atomic.AtomicInteger"));
		}
	}
}