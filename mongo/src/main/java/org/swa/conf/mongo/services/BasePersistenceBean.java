package org.swa.conf.mongo.services;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.swa.conf.business.persistence.BasePersistenceService;
import org.swa.conf.datatypes.AbstractDatatype;
import org.swa.conf.mongo.annotations.DocumentAttributes;
import org.swa.conf.monitoring.StatisticsSource;

import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

@StatisticsSource
public abstract class BasePersistenceBean<T extends AbstractDatatype> implements BasePersistenceService<T> {

	@Inject
	protected Logger										log;

	/** Data journaled in at least 2 servers */
	protected static final WriteConcern	SAFE		= new WriteConcern(2, 0, false, true);
	/** Data journaled in at least 1 servers */
	protected static final WriteConcern	NORMAL	= new WriteConcern(1, 0, false, true);
	/** Data sent to at least 1 servers */
	protected static final WriteConcern	UNSAFE	= new WriteConcern(1, 0, false, false);
	/** Data sent to the socket */
	protected static final WriteConcern	NONE		= new WriteConcern(0, 0, false, false);

	// TODO leider hat die aktuelle mongo version noch wenig inteligenz und wirft bei SAFE aber nur einem einzigen server
	// eine exception, anstatt logisch nachzudenken: OK, er will es in mindestens 2 servers geschrieben haben, aber es
	// gibt eben nur einen, so wird's gewartet, bis das der eine server sicher gespeichert hat ...
	// Möglicher ansatz: dynamischer WriteConcern - Exception abfangen (nicht so viele Server vorhanden wie gewollt) und
	// versuchen eine Stufe runter

	protected WriteConcern							defaultWriteConcern;

	private Class<T>										genericClass;

	private static ObjectId toObjectId(final Object id) {
			return (id != null && id.getClass() != ObjectId.class) ? new ObjectId(id.toString()) : (ObjectId) id;
	}

	@Override
	public T findById(final Object id) {
		this.log.debug("Finding {} by id `{}`", genericClass.getSimpleName(), id.toString());
		final T t = this.getCollection().findOne(toObjectId(id)).as(genericClass);

		// TODO besser wäre den Jackson zu konfigurieren, so dass ObjectId als HexString marshaled wird
		if (t == null)
			return null;
		return (T) t.setId(((ObjectId) t.getId()).toHexString());
	}

	@Override
	public List<T> findAll() {
		this.log.debug("Finding all {}s", genericClass.getSimpleName());

		final List<T> l = new ArrayList<>();

		for (final T t : this.getCollection().find().as(genericClass))
			l.add((T) t.setId(((ObjectId) t.getId()).toHexString()));

		this.log.debug("Found {} {}s", l.size(), genericClass.getSimpleName());

		return l;
	}

	@Override
	public T save(final T t) {
		if (t.getId() == null) {
			t.setId(new ObjectId());
			log.trace("Added new ID {} to {}", ((ObjectId) t.getId()).toHexString(), t);
		} else
			t.setId(toObjectId(t.getId()));
		this.log.debug("Saving {} with data {}", genericClass.getSimpleName(), t);
		this.getCollection().withWriteConcern(this.defaultWriteConcern).save(t);
		return t;
	}

	@Override
	public void remove(final T t) {
		remove(t.getId());
	}

	@Override
	public void remove(final Object id) {
		this._removeById(id);
	}

	@Override
	public boolean exist(final Object id) {
		return this.getCollection().count("{_id:#}", toObjectId(id)) > 0;
	}

	protected WriteResult _remove(final T t) {
		return this._removeById(t.getId());
	}

	// System.out.println("######################:" + this.coll.findOne().map(new ResultHandler<String>() {
	// @Override
	// public String map(final DBObject arg0) {
	// return arg0.toString();
	// }
	// }));

	protected WriteResult _removeById(final Object id) {
		this.log.debug("Removing {} by id `{}`", genericClass.getSimpleName(), id.toString());
		return this.getCollection().withWriteConcern(this.defaultWriteConcern).remove(toObjectId(id));
	}

	abstract public MongoCollection getCollection();

	abstract protected void setGenericClass();

	protected void setGenericClass(final Class<?> genericClass) {

		this.genericClass = (Class<T>) genericClass;

		final DocumentAttributes d = genericClass.getAnnotation(DocumentAttributes.class);
		if (d != null)
			switch (d.criticality()) {
			case HIGH:
				this.defaultWriteConcern = BasePersistenceBean.SAFE;
				break;
			case NORMAL:
				this.defaultWriteConcern = BasePersistenceBean.NORMAL;
				break;
			case LOW:
				this.defaultWriteConcern = BasePersistenceBean.UNSAFE;
				break;
			default:
				this.defaultWriteConcern = BasePersistenceBean.NONE;
			}
		else
			this.defaultWriteConcern = BasePersistenceBean.NONE;
	}
}