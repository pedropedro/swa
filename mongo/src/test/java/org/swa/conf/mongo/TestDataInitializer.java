package org.swa.conf.mongo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.swa.conf.business.persistence.BasePersistenceService;
import org.swa.conf.datatypes.Conference;
import org.swa.conf.datatypes.Location;
import org.swa.conf.mongo.collections.ConferenceCollection;
import org.swa.conf.mongo.collections.LocationCollection;
import org.swa.conf.mongo.producers.ArchiveProducer;
import org.swa.conf.mongo.producers.FongoDbProducer;

@RunWith(Arquillian.class)
public class TestDataInitializer {

	@Deployment
	public static Archive<?> createTestArchive() {
		final WebArchive war = ArchiveProducer.createMongoTestWebArchive();
		war.deleteClass(FongoDbProducer.class);
		return war;
	}

	@Inject
	private BasePersistenceService<Conference> cp;

	@Inject
	private BasePersistenceService<Location> cl;

	private static final List<Location> locations = new ArrayList<>();

	static {
		LocationCollection l;

		l = new LocationCollection();
		l.setName("Frankfurt am Main");
		l.setLatitude(50.1109221);
		l.setLongitude(8.6821267);
		l.setAddress("Germany");
		locations.add(l);

		l = new LocationCollection();
		l.setName("München / Munich");
		l.setLatitude(48.1448353);
		l.setLongitude(11.5580067);
		l.setAddress("Germany");
		locations.add(l);

		l = new LocationCollection();
		l.setName("Praha / Prague");
		l.setLatitude(50.0878114);
		l.setLongitude(14.4204598);
		l.setAddress("Czech Republic");
		locations.add(l);

		l = new LocationCollection();
		l.setName("London");
		l.setLatitude(51.5081289);
		l.setLongitude(-0.1280050);
		l.setAddress("United Kingdom");
		locations.add(l);

		l = new LocationCollection();
		l.setName("Genève / Genf / Geneva");
		l.setLatitude(46.1983922);
		l.setLongitude(6.1422961);
		l.setAddress("Switzerland");
		locations.add(l);
	}

	@Test
	@InSequence(value = 10)
	public void setCityLocations() {
		final List<Location> allLocations = cl.findAll();
		if (allLocations.isEmpty())
			for (final Location l : locations) cl.save(l);
		else {
			locations.clear();
			locations.addAll(allLocations);
		}
	}

	@Test
	@InSequence(value = 50)
	public void setConferences() {

		final Random r = new Random();
		final long dFrom = Utils.parseDate("2000-01-01").getTime();
		final String[] lorems = lorem.split("\\.");
		final int locs = locations.size();
		final int lors = lorems.length;
		final long DAY = 24l * 60l * 60l * 1000l;

		ConferenceCollection c;

		for (int row = 0; row < 500; row++) {

			String desc = "";
			for (int i = r.nextInt(lors), l = r.nextInt(lors); l >= 0; i++, l--) desc += (lorems[i % lors] + ". ");
			desc = desc.trim();

			// Y2K + 20Y, then truncate to midnight
			Date dateFrom = Utils.parseDate(Utils.formatDate(new Date(dFrom + r.nextInt(7300) * DAY)));
			Date dateTo = new Date(dateFrom.getTime() + r.nextInt(14) * DAY);

			c = new ConferenceCollection();
			c.setName(lorems[r.nextInt(lors)]);
			c.setDescription(desc);
			c.setFrom(dateFrom);
			c.setTo(dateTo);
			c.setLocation(locations.get(r.nextInt(locs)));
			cp.save(c);
		}
	}

	/** Generated in MS WORD using:  =lorem(3,16) ENTER */
	private static final String lorem = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.Maecenas " +
			"porttitor congue massa.Fusce posuere, magna sed pulvinar ultricies, purus lectus malesuada libero, " +
			"sit amet commodo magna eros quis urna.Nunc viverra imperdiet enim.Fusce est.Vivamus a tellus." +
			"Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas.Proin " +
			"pharetra nonummy pede.Mauris et orci.Aenean nec lorem.In porttitor.Donec laoreet nonummy augue." +
			"Suspendisse dui purus, scelerisque at, vulputate vitae, pretium mattis, nunc.Mauris eget neque at sem " +
			"venenatis eleifend.Ut nonummy.Fusce aliquet pede non pede.Suspendisse dapibus lorem pellentesque " +
			"magna.Integer nulla.Donec blandit feugiat ligula.Donec hendrerit, felis et imperdiet euismod, purus " +
			"ipsum pretium metus, in lacinia nulla nisl eget sapien.Donec ut est in lectus consequat consequat." +
			"Etiam eget dui.Aliquam erat volutpat.Sed at lorem in nunc porta tristique.Proin nec augue.Quisque " +
			"aliquam tempor magna.Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac " +
			"turpis egestas.Nunc ac magna.Maecenas odio dolor, vulputate vel, auctor ac, accumsan id, felis." +
			"Pellentesque cursus sagittis felis.Pellentesque porttitor, velit lacinia egestas auctor, diam eros " +
			"tempus arcu, nec vulputate augue magna vel risus.Cras non magna vel ante adipiscing rhoncus." +
			"Vivamus a mi.Morbi neque.Aliquam erat volutpat.Integer ultrices lobortis eros.Pellentesque habitant" +
			" morbi tristique senectus et netus et malesuada fames ac turpis egestas.Proin semper, ante vitae " +
			"sollicitudin posuere, metus quam iaculis nibh, vitae scelerisque nunc massa eget pede.Sed velit urna, " +
			"interdum vel, ultricies vel, faucibus at, quam.Donec elit est, consectetuer eget, consequat quis, " +
			"tempus quis, wisi.In in nunc.Class aptent taciti sociosqu ad litora torquent per conubia nostra, " +
			"per inceptos hymenaeos.Donec ullamcorper fringilla eros.Fusce in sapien eu purus dapibus commodo." +
			"Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus.Cras faucibus " +
			"condimentum odio.Sed ac ligula.Aliquam at eros.";
}