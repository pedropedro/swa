package org.swa.conf.monitoring;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class LoggerProducer {

	@Produces
	@Dependent
	private static Logger produceLogger(final InjectionPoint injectionPoint) {
		return LoggerFactory.getLogger(injectionPoint.getMember().getDeclaringClass());
	}
}