package org.swa.conf.business.access.rest;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import org.swa.conf.business.access.rest.impl.ConferenceRestServiceBean;
import org.swa.conf.business.access.rest.impl.EJBExceptionMapper;

@ApplicationScoped
@javax.ws.rs.ApplicationPath("rest")
public class ApplicationPath extends javax.ws.rs.core.Application {

	@Override
	public Set<Class<?>> getClasses() {
		final Set<Class<?>> classes = new HashSet<>();
		classes.add(ConferenceRestServiceBean.class);
		classes.add(EJBExceptionMapper.class);
		return classes;
	}
}