package org.swa.conf.business.access.rest;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import static org.junit.Assert.*;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.extension.rest.client.ArquillianResteasyResource;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.swa.conf.business.access.rest.impl.ConferenceRestServiceBean;
import org.swa.conf.business.access.rest.impl.EJBExceptionMapper;
import org.swa.conf.business.mock.ConferenceCollection;
import org.swa.conf.business.mock.ConferencePersistenceBean;
import org.swa.conf.business.service.ArchiveProducer;
import org.swa.conf.business.service.BaseService;
import org.swa.conf.business.service.ConferenceService;
import org.swa.conf.datatypes.Conference;
import org.swa.conf.datatypes.Location;
import org.swa.conf.datatypes.Talk;
import org.swa.conf.datatypes.validators.ModelValidator;
import org.swa.conf.datatypes.validators.ValidationException;

@RunWith(Arquillian.class)
public class ConferenceRestServiceTest {

	private static final String appPath = "rest";

	@Deployment
	public static Archive<?> createTestArchive() {
		final WebArchive war = ArchiveProducer.createTestWebArchive();
		war.addClasses(ApplicationPath.class, EJBExceptionMapper.class, StringToLong.class);
		war.addClasses(ModelValidator.class, ValidationException.class, PathParamIdValidator.class);
		war.addClasses(BaseService.class, ConferenceService.class, ConferencePersistenceBean.class,
				ConferenceCollection.class);
		war.addClasses(BaseRestService.class, ConferenceRestService.class, ConferenceRestServiceBean.class);
		war.addAsWebInfResource("ejb-jar.xml");
		war.addAsResource("ValidationMessages.properties");
		System.out.println(war.toString(true));
		return war;
	}

	@Inject
	private ConferenceService s;

	private static final long HOUR = 60L * 60L * 1000L;
	private static final long DAY = 24L * HOUR;

	@Test
	@InSequence(value = 1)
	public void setupTestData() {

		final Location l = new Location();
		l.setId(11L);
		l.setName("city");
		l.setAddress("street");

		final List<Talk> talks = new ArrayList<>();
		Talk t = new Talk();
		t.setId(21L);
		t.setName("talk 1");
		t.setFrom(new Date(8L * HOUR));
		t.setTo(new Date(10L * HOUR));
		talks.add(t);
		t = new Talk();
		t.setId(22L);
		t.setName("talk 2");
		t.setFrom(new Date(13L * HOUR));
		t.setTo(new Date(16L * HOUR));
		talks.add(t);

		Conference c = new Conference();
		c.setName("Name 1");
		c.setDescription("description 1");
		c.setFrom(new Date(0));
		c.setTo(new Date(5L * DAY));
		c.setLocation(l);
		c.setTalks(talks);
		s.save(c);
		assertEquals(Long.valueOf(1L), c.getId());

		c = new Conference();
		c.setName("Name 2");
		c.setDescription("description 2");
		c.setFrom(new Date(0));
		c.setTo(new Date(5l * DAY));
		s.save(c);
		assertEquals(Long.valueOf(2L), c.getId());
	}

	@Test
	@InSequence(value = 10)
	@RunAsClient
	// @Produces(MediaType.APPLICATION_XML) // Overrides designed annotation
	public void testGetOne(
			@ArquillianResteasyResource(appPath) final ConferenceRestService resource,
			@ArquillianResource final URL deploymentURL,
			@ArquillianResteasyResource(appPath) final ResteasyWebTarget webTarget) {

		// out of the EJB / CDI container the injected "log" is not available !
		final java.util.logging.Logger clientLog = java.util.logging.Logger.getLogger(getClass().getName());
		clientLog.info("Deployment URL : " + deploymentURL);
		clientLog.info("Web target : " + webTarget);

		// Get the test data through REST back again ...
		final Response rs = resource.findById("1");
		assertEquals(Response.Status.OK, rs.getStatusInfo());
		assertTrue(rs.hasEntity());
		final Conference d = rs.readEntity(Conference.class);
		rs.close();

		assertEquals(Long.valueOf(1), d.getId());
		assertEquals("Name 1", d.getName());

		// final Invocation.Builder invocationBuilder = webTarget.request();
		// invocationBuilder.acceptEncoding("UTF-8");
		// invocationBuilder.accept(MediaType.APPLICATION_ATOM_XML_TYPE);
		// invocationBuilder.header("Authorization","Basic sialala");
		// final Invocation invocation = invocationBuilder.buildPost(Entity.entity("{\"biskupa\":\"?upa\"}",
		// MediaType.APPLICATION_JSON_TYPE));

		// final Response response = invocation.invoke();

		// assertEquals(deploymentURL + "rest/customer", webTarget.getUri().toASCIIString());
		// assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
		// assertEquals(Status.OK, rs.getStatusInfo());
	}

	@Test
	@InSequence(value = 20)
	@RunAsClient
	public void testGetOneWrongParam(@ArquillianResteasyResource(appPath) final ConferenceRestService resource) {

		Response rs = resource.findById("XYZ");
		assertEquals(Response.Status.BAD_REQUEST, rs.getStatusInfo());
		assertTrue(rs.hasEntity());
		assertTrue(rs.readEntity(String.class).contains(
				"\"message\":\"The id must be a valid decimal or hexadecimal number\",\"value\":\"XYZ\""));
		rs.close();

		rs = resource.findById("xFFFFFF");
		assertEquals(Response.Status.NOT_FOUND, rs.getStatusInfo());
		rs.close();

		rs = resource.findById(""); // !!!!!!!!!!!!!!!!!! Translates actually to getAll() !!!!!!!!!!!!!!!!!!
		assertEquals(Response.Status.OK, rs.getStatusInfo());
		assertTrue(rs.hasEntity());
		final List<Conference> d = rs.readEntity(List.class);
		assertEquals(2, d.size());
		rs.close();
	}

	@Test
	@InSequence(value = 30)
	@RunAsClient
	public void testGetAll(@ArquillianResteasyResource(appPath) final ConferenceRestService resource) {

		final Response rs = resource.find(null, null, null, null);
		assertEquals(Response.Status.OK, rs.getStatusInfo());
		assertTrue(rs.hasEntity());
		final List<Conference> all = rs.readEntity(List.class);
		assertEquals(2, all.size());
	}

	@Test
	@InSequence(value = 40)
	@RunAsClient
	public void testDeleteNonExistent(@ArquillianResteasyResource(appPath) final ConferenceRestService resource) {

		final Response rs = resource.delete("XFFFFFF");
		assertEquals(Response.Status.OK, rs.getStatusInfo());
		assertNotNull(rs.getLink("parent"));
		assertEquals("/" + ArchiveProducer.APP + "/" + appPath + "/"
				+ ConferenceRestService.PATH_COLLECTION, rs.getLink("parent").getUri().getPath());

		rs.close();
	}

	@Test
	@InSequence(value = 50)
	@RunAsClient
	public void testSaveNewEntity(@ArquillianResteasyResource(appPath) final ConferenceRestService resource) {

		final Conference c = new Conference();
		c.setName(null); // The field is @NotNull !
		c.setDescription("description 2"); // second row with such a description
		c.setFrom(new Date(10L * DAY));
		c.setTo(new Date(17L * DAY));

		Response rs = resource.save(c);
		assertEquals(Response.Status.BAD_REQUEST, rs.getStatusInfo());
		assertEquals("{\"!\":\"Constraint violation(s):\\nconference.name: may not be null\"}",
				rs.readEntity(String.class));
		rs.close();

		c.setName("Name 3");
		rs = resource.save(c);
		assertEquals(Response.Status.CREATED, rs.getStatusInfo());
		assertNotNull(rs.getLink("self"));
		assertEquals("/" + ArchiveProducer.APP + "/" + appPath + "/"
				+ ConferenceRestService.PATH_COLLECTION + "/3", rs.getLink("self").getUri().getPath());
		rs.close();

		rs = resource.findById("3");
		assertEquals(Response.Status.OK, rs.getStatusInfo());
		assertTrue(rs.hasEntity());
		final Conference d = rs.readEntity(Conference.class);
		assertEquals(Long.valueOf(3), d.getId());
		assertEquals("Name 3", d.getName());
		rs.close();
	}

	@Test
	@InSequence(value = 60)
	@RunAsClient
	public void testFindByQuery(@ArquillianResteasyResource(appPath) final ConferenceRestService resource) {

		final String query = "name=='Name 1' and from >= 2015-01-01";

		final Response rs = resource.find(null, null, null, query);
		assertEquals(Response.Status.OK, rs.getStatusInfo());
		assertTrue(rs.hasEntity());
		final List<Conference> all = rs.readEntity(List.class);
		assertEquals(3, all.size());
		assertEquals(query, rs.getLink("next").getParams().get(BaseRestService.QRY_PARAM_QUERY));
		assertEquals("1", rs.getLink("next").getParams().get(BaseRestService.QRY_PARAM_PAGE));
		assertEquals(query, rs.getLink("prev").getParams().get(BaseRestService.QRY_PARAM_QUERY));
		assertEquals("1", rs.getLink("prev").getParams().get(BaseRestService.QRY_PARAM_PAGE));
		rs.close();
	}

	@Test
	@InSequence(value = 70)
	@RunAsClient
	public void testFindWithSort(@ArquillianResteasyResource(appPath) final ConferenceRestService resource) {

		final Response rs = resource.find(null, null, "-description+name", null);
		assertEquals(Response.Status.OK, rs.getStatusInfo());
		assertTrue(rs.hasEntity());
		final List<Conference> all = rs.readEntity(List.class);
		assertEquals(3, all.size());
		rs.close();
	}
}