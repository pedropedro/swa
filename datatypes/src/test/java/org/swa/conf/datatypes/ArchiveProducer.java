package org.swa.conf.datatypes;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

public class ArchiveProducer {

	public static PomEquippedResolveStage pers = Maven.configureResolver().workOffline().loadPomFromFile("pom.xml");

	public static WebArchive createTestArchive() {
		final WebArchive war = ShrinkWrap.create(WebArchive.class, "datatypes-ejb.war");
		war.addPackages(true, AbstractDatatype.class.getPackage());
		war.addAsWebInfResource("empty-beans.xml", "beans.xml");
		war.addAsResource("ValidationMessages.properties");
		System.out.println(war.toString(true));
		return war;
	}
}