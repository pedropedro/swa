package org.swa.conf.business.service;

import javax.inject.Inject;

import static org.junit.Assert.*;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.swa.conf.business.mock.ConferenceCollection;
import org.swa.conf.business.mock.ConferencePersistenceBean;
import org.swa.conf.datatypes.Conference;

@RunWith(Arquillian.class)
public class ConferenceServiceTest {

	@Deployment
	public static Archive<?> createTestArchive() {
		final WebArchive war = ArchiveProducer.createTestWebArchive();
		war.addClass(BaseService.class);
		war.addClass(ConferenceService.class);
		war.addClass(ConferenceServiceTest.class);
		war.addClass(ConferenceCollection.class);
		war.addClass(ConferencePersistenceBean.class);
		war.addAsWebInfResource("ejb-jar.xml");
		System.out.println(war.toString(true));
		return war;
	}

	@Inject
	private ConferenceService s;

	@Test
	@InSequence(value = 10)
	public void notFound() {
		final Conference rec = s.findById(Long.valueOf("1"));
		assertNull(rec);
	}

	@Test
	@InSequence(value = 20)
	public void crud() {

		final Conference o = new Conference();
		o.setName("name");
		o.setId(null); // the persistence layer must take care !

		// Create
		final Conference oCreated = s.save(o);
		assertNotNull(oCreated.getId());

		// Read
		Conference oRead = s.findById(oCreated.getId());
		assertNotNull(oRead);
		assertEquals(oCreated, oRead);

		// Update
		oCreated.setName("Name 2");
		final Conference oUpdated = s.save(oCreated);

		oRead = s.findById(oUpdated.getId());
		assertNotNull(oRead);
		assertEquals(oUpdated, oRead);
		// in our mock database is the same object !
		// assertNotEquals(oUpdated, oCreated);

		// Delete
		s.remove(oUpdated);
		assertNull(s.findById(oUpdated.getId()));
	}
}