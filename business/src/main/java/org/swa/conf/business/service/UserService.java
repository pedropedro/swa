package org.swa.conf.business.service;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jasypt.digest.StandardStringDigester;
import org.slf4j.Logger;
import org.swa.conf.business.persistence.BasePersistenceService;
import org.swa.conf.datatypes.User;

@Stateless
public class UserService {

	@Inject
	private Logger log;

	@Inject
	private BasePersistenceService<User> persistence;

	public User findById(final Long id) {
		return persistence.findById(id);
	}

	public List<User> findAll() {
		return persistence.findAll();
	}

	public User save(final User t) {
		return persistence.save(t);
	}

	public void remove(final User t) {
		persistence.remove(t);
	}

	private final StandardStringDigester digester = new StandardStringDigester();

	public String digest(final String plain) {
		return digester.digest(plain);
	}

	public boolean matches(final String plain, final String digest) {
		return digester.matches(plain, digest);
	}

	// TODO when creating a new user (PUT) or (re)setting the password, digest the plain password

	@PostConstruct
	private void init() {
		digester.setAlgorithm("SHA-512");
		digester.setIterations(100000);
		digester.setSaltSizeBytes(16);
		digester.initialize();
	}
}