package org.swa.conf.datatypes;

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
public class LocationTest {

	@Deployment
	public static Archive<?> createTestArchive() {
		return ArchiveProducer.createTestArchive();
	}

	@Inject
	private ModelValidator validator;

	@Test
	public void geoAttributesTest() {

		final CloneableLocation l = new CloneableLocation();
		l.setId(1l);
		l.setName("city");
		l.setAddress("street");
		l.setLongitude(180d);

		validator.validate(l);

		try {
			validator.validate(l.clone().setLongitude(null));
			fail("Expected validation exception");
		} catch (final ValidationException e) {
			assertTrue(e.getMessage(), e.getMessage().endsWith("\n" + "location.longitude: may not be null"));
		}
		try {
			validator.validate(l.clone().setLongitude(180.0000001));
			fail("Expected validation exception");
		} catch (final ValidationException e) {
			assertTrue(e.getMessage(), e.getMessage().endsWith(
					"\n" + "location.longitude: value must be within range -180.000000 to +180.000000"));
			validator.validate(l.clone().setLongitude(-180d));
		}
		try {
			validator.validate(l.clone().setLatitude(90.0000001));
			fail("Expected validation exception");
		} catch (final ValidationException e) {
			assertTrue(e.getMessage(), e.getMessage().endsWith(
					"\n" + "location.latitude: value must be within range -90.000000 to +90.000000"));
			validator.validate(l.clone().setLatitude(-90d));
		}
		try {
			validator.validate(l.clone().setLatitude(-90.0000001).setLongitude(-180.0000001));
			fail("Expected validation exception");
		} catch (final ValidationException e) {
			assertTrue(e.getMessage(), e.getMessage().contains(
					"\n" + "location.longitude: value must be within range -180.000000 to +180.000000"));
			assertTrue(e.getMessage(), e.getMessage().contains(
					"\n" + "location.latitude: value must be within range -90.000000 to +90.000000"));
		}
	}
}