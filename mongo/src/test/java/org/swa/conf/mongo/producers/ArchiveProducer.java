package org.swa.conf.mongo.producers;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.swa.conf.configuration.EnvironmentEntriesHolder;
import org.swa.conf.monitoring.LoggerProducer;

public class ArchiveProducer {

	public static PomEquippedResolveStage pers = Maven.configureResolver().workOffline().loadPomFromFile("pom.xml");

	public static WebArchive createMongoTestWebArchive() {

		final WebArchive war = ShrinkWrap.create(WebArchive.class, "mongo-ejb.war");
		war.addPackages(true, "org.swa.conf.mongo");
		war.addPackages(false, "org.swa.conf.datatypes");
		war.addPackages(false, "org.swa.conf.business.persistence");
		war.addClass(EnvironmentEntriesHolder.class);
		war.addClass(LoggerProducer.class);
		war.addAsLibraries(pers.resolve("org.mongodb:mongo-java-driver").withTransitivity().asFile());
		war.addAsLibraries(pers.resolve("org.jongo:jongo").withTransitivity().asFile());
		war.addAsLibraries(pers.resolve("com.github.fakemongo:fongo").withTransitivity().asFile());
		war.addAsWebInfResource("mongo-ejb-jar.xml", "ejb-jar.xml");
		war.addAsWebInfResource("empty-beans.xml", "beans.xml");
		System.out.println(war.toString(true));
		return war;
	}

	public static WebArchive createFullBlownTestWebArchive() {

		final WebArchive war = ShrinkWrap.create(WebArchive.class, "conf-ejb.war");
		war.addPackages(true, "org.swa.conf");
		war.addAsLibraries(pers.resolve("org.mongodb:mongo-java-driver").withTransitivity().asFile());
		war.addAsLibraries(pers.resolve("org.jongo:jongo").withTransitivity().asFile());
		war.addAsLibraries(pers.resolve("com.github.fakemongo:fongo").withTransitivity().asFile());
		war.addAsWebInfResource("mongo-ejb-jar.xml", "ejb-jar.xml");
		war.addAsWebInfResource("interceptor-beans.xml", "beans.xml");
		// war.merge( ShrinkWrap.create(GenericArchive.class).as(ExplodedImporter.class).importDirectory("WEBAPP_SRC")
		// .as(GenericArchive.class), "/", Filters.include(".*\\.(xhtml|html|css|js|png)$"));
		// war.addAsWebResource(new File("TEST_RESOURCES", "/pages/test-logon.xhtml"), "/templates/logon.xhtml");
		System.out.println(war.toString(true));
		return war;
	}
}