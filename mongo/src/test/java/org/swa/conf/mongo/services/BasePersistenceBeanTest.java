package org.swa.conf.mongo.services;

import java.util.List;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.swa.conf.business.persistence.BasePersistenceService;
import org.swa.conf.datatypes.Conference;
import org.swa.conf.mongo.Utils;
import org.swa.conf.mongo.collections.ConferenceCollection;
import org.swa.conf.mongo.producers.ArchiveProducer;

@RunWith(Arquillian.class)
public class BasePersistenceBeanTest {

	@Deployment
	public static Archive<?> createTestArchive() {
		return ArchiveProducer.createMongoTestWebArchive();
	}

	@Inject
	private BasePersistenceService<Conference> persistence;

	@Test
	@InSequence(value = 10)
	public void findByIdTest() {

		Assert.assertNull(persistence.findById(-1L));

		Assert.assertNull(persistence.findById(1L));
		ConferenceCollection c = new ConferenceCollection();
		c.setId(1L);
		c.setName("Name 1");
		persistence.save(c);
		c = new ConferenceCollection();
		c.setId(2L);
		c.setName("Name 2");
		persistence.save(c);

		Assert.assertNotNull(persistence.findById(1L));
		Assert.assertEquals("Name 1", persistence.findById(1L).getName());
	}

	@Test
	@InSequence(value = 20)
	public void findAllTest() {
		final List<Conference> l = persistence.findAll();
		Assert.assertNotNull(l);
		Assert.assertEquals(2, l.size());
	}

	@Test
	@InSequence(value = 30)
	public void findTest() {
		final List<Conference> l = persistence.find(null, null, null, null);
		Assert.assertNotNull(l);
		Assert.assertEquals(2, l.size());
		Assert.assertArrayEquals(l.toArray(), persistence.findAll().toArray());

		// Non null query tests are in JongoRsqlVisitorTest
	}

	@Test
	@InSequence(value = 40)
	public void saveTest() {
		final ConferenceCollection c = new ConferenceCollection();
		c.setId(null);
		c.setName("Name 3");
		persistence.save(c);
		Assert.assertNotNull(c.getId());

		// Read
		final Conference c2 = persistence.findById(c.getId());
		Assert.assertNotNull(c2);
		Assert.assertEquals(c, c2);
	}

	@Test
	@InSequence(value = 50)
	public void removeTest() {

		final Long l = 100L;

		final ConferenceCollection c = new ConferenceCollection();
		c.setId(l);
		c.setName("Name 100");
		persistence.save(c);

		Assert.assertNotNull(persistence.findById(l));

		persistence.remove(c);

		Assert.assertNull(persistence.findById(l));
	}

	@Test
	@InSequence(value = 60)
	public void removeByIdTest() {

		final Long l = 100L;

		final ConferenceCollection c = new ConferenceCollection();
		c.setId(l);
		c.setName("Name 100");
		persistence.save(c);

		Assert.assertNotNull(persistence.findById(l));

		persistence.remove(l);

		Assert.assertNull(persistence.findById(l));
	}

	@Test
	@InSequence(value = 70)
	public void existsTest() {
		Assert.assertTrue(persistence.exist(1L));
		Assert.assertFalse(persistence.exist(-1L));
	}

	@Test
	@InSequence(value = 80)
	public void countTest() {
		Assert.assertEquals(3, persistence.count(null));
	}

	@Test
	@InSequence(value = 90)
	public void sortByTest() {

		// clean collection
		for (final Conference c : persistence.find(null, null, null, null)) persistence.remove(c);

		ConferenceCollection c = new ConferenceCollection();
		c.setDescription("Description 1");
		c.setFrom(Utils.parseDate("2000-06-01"));
		c.setName("Name 1");
		c.setTo(Utils.parseDate("2000-06-11"));
		persistence.save(c);
		c = new ConferenceCollection();
		c.setDescription("Description 2");
		c.setFrom(Utils.parseDate("2000-07-01"));
		c.setName("Name 2");
		c.setTo(Utils.parseDate("2000-07-11"));
		persistence.save(c);
		c = new ConferenceCollection();
		c.setDescription("Description 2");
		c.setFrom(Utils.parseDate("2000-08-01"));
		c.setName("Name 3");
		c.setTo(Utils.parseDate("2000-08-11"));
		persistence.save(c);

		// no sort
		List<Conference> conferenceList = persistence.find(null, null, null, null);
		Assert.assertEquals(3, conferenceList.size());
		Assert.assertEquals("Name 1", conferenceList.get(0).getName());
		Assert.assertEquals("Name 2", conferenceList.get(1).getName());
		Assert.assertEquals("Name 3", conferenceList.get(2).getName());

		// sort by description DESC, name ASC
		conferenceList = persistence.find(null, null, null, "description-name+");
		Assert.assertEquals(3, conferenceList.size());
		Assert.assertEquals("Name 2", conferenceList.get(0).getName());
		Assert.assertEquals("Name 3", conferenceList.get(1).getName());
		Assert.assertEquals("Name 1", conferenceList.get(2).getName());
	}


	private final int pageLength = 10;

	private int skip(final int page) {
		return (page - 1) * pageLength;
	}

	@Test
	@InSequence(value = 100)
	public void paginationTest() {

		// clean collection
		for (final Conference c : persistence.find(null, null, null, null)) persistence.remove(c);

		final int rowCount = 42;

		for (int i = 0; i < rowCount; i++) {
			final ConferenceCollection c = new ConferenceCollection();
			c.setName("Name " + i);
			persistence.save(c);
		}

		// no pagination
		List<Conference> conferenceList = persistence.find(null, null, null, null);
		Assert.assertEquals(rowCount, conferenceList.size());
		for (int i = 0; i < rowCount; i++)
			Assert.assertEquals("Name " + i, conferenceList.get(i).getName());


		// zero page (doesn't exists) delivers the first one
		int currentPage = 0;
		conferenceList = persistence.find(null, skip(currentPage), pageLength, null);
		Assert.assertEquals(pageLength, conferenceList.size());
		for (int i = 0; i < pageLength; i++)
			Assert.assertEquals("Name " + i, conferenceList.get(i).getName());

		// negative page (doesn't exists) delivers the first one
		currentPage = -1;
		conferenceList = persistence.find(null, skip(currentPage), pageLength, null);
		Assert.assertEquals(pageLength, conferenceList.size());
		for (int i = 0; i < pageLength; i++)
			Assert.assertEquals("Name " + i, conferenceList.get(i).getName());

		// first page
		currentPage = 1;
		conferenceList = persistence.find(null, skip(currentPage), pageLength, null);
		Assert.assertEquals(pageLength, conferenceList.size());
		for (int i = 0; i < pageLength; i++)
			Assert.assertEquals("Name " + i, conferenceList.get(i).getName());

		// third page
		currentPage = 3;
		conferenceList = persistence.find(null, skip(currentPage), pageLength, null);
		Assert.assertEquals(pageLength, conferenceList.size());
		for (int i = 0; i < pageLength; i++)
			Assert.assertEquals("Name " + ((currentPage - 1) * pageLength + i), conferenceList.get(i).getName());

		// last page
		currentPage = (int) Math.ceil((float) rowCount / pageLength);
		conferenceList = persistence.find(null, skip(currentPage), pageLength, null);
		Assert.assertEquals(rowCount % pageLength, conferenceList.size());
		for (int i = 0; i < rowCount % pageLength; i++)
			Assert.assertEquals("Name " + ((currentPage - 1) * pageLength + i), conferenceList.get(i).getName());

		// non existent page
		currentPage = 1000;
		conferenceList = persistence.find(null, skip(currentPage), pageLength, null);
		Assert.assertEquals(0, conferenceList.size());
	}
}