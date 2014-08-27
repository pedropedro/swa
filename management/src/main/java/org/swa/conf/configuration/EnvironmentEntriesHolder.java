package org.swa.conf.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJBException;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.slf4j.Logger;

// Could have been just @ApplicationScoped if not mentioned in ejb-jar.xml => rendering it to an EJB
// @ApplicationScoped
@javax.ejb.Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
/** Convenience bridge for the DevOps between our application and deployment configuration in the ejb-jar.xml descriptor. */
public class EnvironmentEntriesHolder {

	public final String						ENV_passwordChangePeriodInDays	= "passwordChangePeriodInDays";

	@Inject
	private Logger								log;

	private javax.naming.Context	ctx;
	private Map<String, Object>		cache;
	private Set<String>						entryNames;

	@PostConstruct
	private void Init() {

		cache = new HashMap<>();
		entryNames = new HashSet<>();

		try {
			ctx = (javax.naming.Context) new InitialContext().lookup("java:comp/env");

			log.info("Configured with following values:");
			for (final NamingEnumeration<Binding> e = ctx.listBindings("/env"); e.hasMoreElements();) {

				final Binding b = e.nextElement();

				if (b.getClassName().startsWith("java.lang.")) {
					entryNames.add(b.getName());
					cache.put(b.getName(), b.getObject());
					log.info("(cached) {}", b);
				} else
					log.info("{}", b);
			}

			entryNames = Collections.unmodifiableSet(entryNames);

		} catch (final NamingException e) {
			throw new EJBException(e);
		}
	}

	public Set<String> getEntryNames() {
		return entryNames;
	}

	/**
	 * Not synchronized setter for new runtime environment entry values - supposed to be used by a MXBean ! It cannot be
	 * made package-visible, WELD doesn't like it
	 */
	public void setNewRuntimeValue(final String name, final Object value) {

		if (!entryNames.contains(name))
			throw new EJBException("Unknown environment entry '" + name + "' !");

		final Object o = getByName(name);

		log.info("Amending ENV '{}' from '{}' to new value '{}'", name, o, value);

		if (o != null && value != null && o.getClass() != value.getClass())
			throw new EJBException("Old and new value have different types !");

		cache.put(name, value);
	}

	private Object getByName(final String name) {

		final Object o = cache.get(name);

		if (o == null && !entryNames.contains(name))
			throw new EJBException("Unknown environment entry '" + name + "' !");

		return o;
	}

	public <T> T getByName(final Class<T> t, final String name) {
		final T r = t.cast(getByName(name));
		log.debug("Retrieved '{}'::'{}'", name, r);
		return r;
	}

	public Integer getInteger(final String name) {
		return getByName(Integer.class, name);
	}

	public Long getLong(final String name) {
		return getByName(Long.class, name);
	}

	public String getString(final String name) {
		return getByName(String.class, name);
	}
}