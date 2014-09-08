package org.swa.conf.mongo.services;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.swa.conf.business.persistence.BasePersistenceService;
import org.swa.conf.datatypes.AbstractDatatype;
import org.swa.conf.mongo.annotations.DocumentAttributes;
import org.swa.conf.monitoring.StatisticsSource;

@StatisticsSource
public abstract class BasePersistenceBean<T extends AbstractDatatype> implements BasePersistenceService<T> {

	@Inject
	protected Logger log;

	/** Data journaled in at least 2 servers */
	protected static final WriteConcern SAFE = new WriteConcern(2, 0, false, true);
	/** Data journaled in at least 1 servers */
	protected static final WriteConcern NORMAL = new WriteConcern(1, 0, false, true);
	/** Data sent to at least 1 servers */
	protected static final WriteConcern UNSAFE = new WriteConcern(1, 0, false, false);
	/** Data sent to the socket */
	protected static final WriteConcern NONE = new WriteConcern(0, 0, false, false);

	// TODO leider hat die aktuelle mongo version noch wenig inteligenz und wirft bei SAFE aber nur einem einzigen
	// server eine exception, anstatt logisch nachzudenken: OK, er will es in mindestens 2 servers geschrieben haben,
	// aber es gibt eben nur einen, so wird's gewartet, bis das der eine server sicher gespeichert hat ...
	// Möglicher ansatz: dynamischer WriteConcern - Exception abfangen (nicht so viele Server vorhanden wie gewollt)
	// und versuchen eine Stufe runter

	protected WriteConcern defaultWriteConcern;

	private Class<T> genericClass;

	private static final int NANO_MASK = 0xFFFFF;
	private static final int NANO_SHIFT = 20;

	@Override
	public T findById(final Long id) {

		log.debug("Finding {} by id {}", genericClass.getSimpleName(), Long.toHexString(id));
		return getCollection().findOne("{_id:#}", id).as(genericClass);
	}

	@Override
	public List<T> findAll() {

		log.debug("Finding all {}s", genericClass.getSimpleName());

		final List<T> l = new ArrayList<>();

		for (final T t : getCollection().find().as(genericClass))
			l.add(t);

		log.debug("Found {} {}s", l.size(), genericClass.getSimpleName());

		return l;
	}

	@Override
	public T save(final T t) {
		if (t.getId() == null) {
			t.setId(getId());
			log.trace("Added new ID {} to {}", Long.toHexString(t.getId()), t);
		}
		log.debug("Saving {} with data {}", genericClass.getSimpleName(), t);
		getCollection().withWriteConcern(defaultWriteConcern).save(t);
		return t;
	}

	@Override
	public void remove(final T t) {
		remove(t.getId());
	}

	@Override
	public void remove(final Long id) {
		_removeById(id);
	}

	@Override
	public boolean exist(final Long id) {
		return getCollection().count("{_id:#}", id) > 0;
	}

	protected WriteResult _remove(final T t) {
		return _removeById(t.getId());
	}

	// System.out.println("######################:" + this.coll.findOne().map(new ResultHandler<String>() {
	// @Override
	// public String map(final DBObject arg0) {
	// return arg0.toString();
	// }
	// }));

	protected WriteResult _removeById(final Long id) {
		log.debug("Removing {} by id `{}`", genericClass.getSimpleName(), Long.toHexString(id));
		return getCollection().withWriteConcern(defaultWriteConcern).remove("{_id:#}", id);
	}

	protected abstract MongoCollection getCollection();

	protected abstract void setGenericClass();

	protected void setGenericClass(final Class<?> genericClass) {

		this.genericClass = (Class<T>) genericClass;

		final DocumentAttributes d = genericClass.getAnnotation(DocumentAttributes.class);
		if (d != null)
			switch (d.criticality()) {
				case HIGH:
					defaultWriteConcern = SAFE;
					break;
				case NORMAL:
					defaultWriteConcern = NORMAL;
					break;
				case LOW:
					defaultWriteConcern = UNSAFE;
					break;
				default:
					defaultWriteConcern = NONE;
			}
		else
			defaultWriteConcern = NONE;
	}

	private Long getId() {
		return (System.nanoTime() & NANO_MASK) | (System.currentTimeMillis() << NANO_SHIFT);
	}
}