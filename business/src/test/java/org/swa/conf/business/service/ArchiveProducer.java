package org.swa.conf.business.service;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.swa.conf.business.persistence.BasePersistenceService;
import org.swa.conf.configuration.EnvironmentEntriesHolder;
import org.swa.conf.datatypes.AbstractDatatype;
import org.swa.conf.monitoring.LoggerProducer;

public class ArchiveProducer {

	public static final String						APP		= "business-ejb";

	public static PomEquippedResolveStage	pers	= Maven.configureResolver().workOffline().loadPomFromFile("pom.xml");

	public static WebArchive createTestWebArchive() {
		final WebArchive war = ShrinkWrap.create(WebArchive.class, ArchiveProducer.APP + ".war");
		war.addPackage(AbstractDatatype.class.getPackage());
		war.addPackage(BasePersistenceService.class.getPackage());
		war.addClass(EnvironmentEntriesHolder.class);
		war.addClass(LoggerProducer.class);
		war.addAsWebInfResource("empty-beans.xml", "beans.xml");
		return war;
	}
}