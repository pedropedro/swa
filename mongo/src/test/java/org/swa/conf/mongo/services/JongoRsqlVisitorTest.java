package org.swa.conf.mongo.services;

import java.util.List;
import javax.inject.Inject;

import com.mongodb.DBObject;
import cz.jirutka.rsql.parser.RSQLParser;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.swa.conf.datatypes.Conference;
import org.swa.conf.mongo.DataLoader;
import org.swa.conf.mongo.producers.ArchiveProducer;

@RunWith(Arquillian.class)
public class JongoRsqlVisitorTest {

	@Deployment
	public static Archive<?> createTestArchive() {
		final Archive<WebArchive> a = ArchiveProducer.createMongoTestWebArchive();
		final Package p = SpeakerMassTest.class.getPackage();
		((WebArchive) a).addAsResource(p, "JongoRsqlVisitorTest#test1.json");
		return a;
	}

	@Inject
	private DataLoader l;

	@Inject
	private ConferencePersistenceLocalBean localBeanDbReader;

	@Inject
	private Logger log;

	@Test
	@RunAsClient
	@InSequence(value = 10)
	public void queryTest() {

		final String query = "name==('Name 1') and from >= 2015-01-01 " +
				"or desc==Ab*ef and list=in=(red,blue,green) and person.x=out=(a,b)";

		final String mongoQuery = new RSQLParser().parse(query).accept(new JongoRsqlVisitor()).toString();
		System.out.println(mongoQuery);
		Assert.assertEquals("{$or:[{$and:[{'name':'Name 1'},{'from':{$gte:{'$date':'2015-01-01T00:00:00.000Z'}}}]}," +
				"{$and:[{'desc':{$regex:'Ab.*ef',$options:'i'}},{'list':{$in:['red','blue','green']}}," +
				"{'person.x':{$nin:['a','b']}}]}]}", mongoQuery);
	}

	@Test
	@InSequence(value = 20)
	public void fongoQueryTest() {

		l.load("conference", DataLoader.Strategy.SET, "test1");

		for (final DBObject dbo : localBeanDbReader.getCollection().getDBCollection().find())
			log.debug("{}", dbo);

		List<Conference> c = localBeanDbReader.find(new RSQLParser().parse("name==name1"), null, null, null);
		Assert.assertNotNull(c);
		Assert.assertEquals(1, c.size());
		Assert.assertEquals("name1", c.get(0).getName());

		c = localBeanDbReader.find(new RSQLParser().parse("name==na*"), null, null, null);
		Assert.assertNotNull(c);
		Assert.assertEquals(3, c.size());

		c = localBeanDbReader.find(new RSQLParser().parse("from > 2000-01-01"), null, null, null);
		Assert.assertNotNull(c);
		System.out.println(c);
		Assert.assertEquals(1, c.size());
		Assert.assertEquals("name2", c.get(0).getName());

		c = localBeanDbReader.find(new RSQLParser().parse("from > 2000-01-01T12:34:56.123Z"), null, null, null);
		Assert.assertNotNull(c);
		System.out.println(c);
		Assert.assertEquals(1, c.size());
		Assert.assertEquals("name2", c.get(0).getName());

		c = localBeanDbReader.find(new RSQLParser().parse("from == 2000-01-01"), null, null, null);
		Assert.assertNotNull(c);
		System.out.println(c);
		Assert.assertEquals(1, c.size());
		Assert.assertEquals("name1", c.get(0).getName());

		c = localBeanDbReader.find(new RSQLParser().parse("from != 2000-01-01"), null, null, null);
		Assert.assertNotNull(c);
		System.out.println(c);
		Assert.assertEquals(2, c.size());
		for (final Conference conference : c) Assert.assertNotEquals("name1", conference.getName());
	}
}