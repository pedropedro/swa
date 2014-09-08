package org.swa.conf.mongo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.DBRefBase;
import com.mongodb.util.JSON;
import org.bson.BSONObject;
import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.swa.conf.mongo.annotations.NamedCollection;

@Dependent
public class DataLoader {

	@Inject
	protected Logger log;

	@Inject
	@Any
	private Instance<MongoCollection> imc;

	private final Class<?> testingClass;

	@Inject
	public DataLoader(final InjectionPoint ip) {
		if (ip == null) {
			// no logger available so faaar !
			System.err.print("No InjectionPoint !");
			throw new Error();
		}
		if (ip.getMember() == null) {
			System.err.print("No InjectionPoint.getMember() !");
			throw new Error();
		}
		if (ip.getMember().getDeclaringClass() == null) {
			System.err.print("No InjectionPoint.getMember().getDeclaringClass() !");
			throw new Error();
		}

		testingClass = ip.getMember().getDeclaringClass();
	}

	/** Convenience wrapper for load("collectionName", DataLoader.Strategy.TRIM, null) */
	public void trim(final String collectionName) {
		load(collectionName, Strategy.TRIM, null);
	}

	/**
	 * Populate given collection with data from a file. The file has to be in classpath of the test class invoking this
	 * method (and having @Injected the {@link DataLoader} first). The real name must be the simple name of the test
	 * class plus the # plus the <code>fileName</code>: running tests from the class org.swa.MyMongoTest and given
	 * parameter <code>fileName</code> == 'abcd' the test data will be pulled from a file org/swa/MyMongoTest#abcd.json
	 * <p/>
	 * The file structure must be a valid JSON format:
	 * <p/>
	 * <pre>
	 * { "any key, for documentation the collection name as string" :
	 *   [
	 *     { "_id" : "entity 1", "field1" : "a string",   "field2" : true },
	 *     { "_id" : "entity 3", "field1" : "a string 2", "field2" : true },
	 *     { "_id" : "entity 2", "field1" : "a string 3", "field2" : false, "field3" : 42 }
	 *   ]
	 * }
	 * </pre>
	 */
	public void load(final String collectionName, final Strategy strategy, final String fileName) {

		// let the CollectionProducer inject the right collection
		final NamedCollectionQualifier q = new NamedCollectionQualifier(collectionName);
		final MongoCollection mc = imc.select(q).get();

		if (strategy == Strategy.SET || strategy == Strategy.TRIM) {
			log.debug("Removing all data in the collection '{}'", mc);
			mc.remove();
		}

		if (strategy == Strategy.TRIM)
			return;

		final String fullFileName = testingClass.getSimpleName() + "#" + fileName + ".json";
		log.debug("Pulling test data from {}", fullFileName);

		try (
				final InputStream resourceAsStream = testingClass.getResourceAsStream(fullFileName);
				final BufferedReader r = new BufferedReader(new InputStreamReader(resourceAsStream, "UTF-8"))
		) {

			final StringBuilder readData = new StringBuilder();
			String readLine;
			while ((readLine = r.readLine()) != null) {
				readData.append(readLine);
			}

			final String jsonData = readData.toString();
			final DBObject parsedData = (DBObject) JSON.parse(jsonData);
			final BasicDBList documents = (BasicDBList) parsedData.get(parsedData.keySet().iterator().next());

			for (final Object dataObject : documents) {
				for (final String key : ((BSONObject) dataObject).keySet()) {
					final Object data = ((BSONObject) dataObject).get(key);
					if (data instanceof DBRef) {
						((BSONObject) dataObject).put(key, new DBRef(mc.getDBCollection().getDB(),
								((DBRefBase) data).getRef(),
								((DBRefBase) data).getId()));
					}
				}
				mc.insert(dataObject);
			}
		} catch (final IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Compares the given collection (sorted by '_id') with the file the/path/to/my/class/MyMongoTest#abcd-expected
	 * .json, given that <code>fileName</code> is 'abcd'.
	 */
	public boolean match(final String collectionName, final String fileName) {

		// let the CollectionProducer inject the right collection
		final NamedCollectionQualifier q = new NamedCollectionQualifier(collectionName);
		final MongoCollection mc = imc.select(q).get();

		final String fullFileName = testingClass.getSimpleName() + "#" + fileName + "-expected.json";
		log.debug("Pulling test data from {}", fullFileName);

		try (
				final InputStream resourceAsStream = testingClass.getResourceAsStream(fullFileName);
				final BufferedReader r = new BufferedReader(new InputStreamReader(resourceAsStream, "UTF-8"))
		) {

			final StringBuilder readData = new StringBuilder();
			String readLine;
			while ((readLine = r.readLine()) != null) {
				readData.append(readLine);
			}

			final String jsonData = readData.toString();
			final DBObject parsedData = (DBObject) JSON.parse(jsonData);
			final BasicDBList documents = (BasicDBList) parsedData.get(parsedData.keySet().iterator().next());

			final DBCursor c = mc.getDBCollection().find().sort(new BasicDBObject("_id", 1));

			for (final Object mustBe : documents) {
				for (final String key : ((BSONObject) mustBe).keySet()) {
					final Object data = ((BSONObject) mustBe).get(key);
					if (data instanceof DBRef) {
						((BSONObject) mustBe).put(key, new DBRef(mc.getDBCollection().getDB(),
								((DBRefBase) data).getRef(),
								((DBRefBase) data).getId()));
					}
				}

				if (c.hasNext()) {
					final DBObject is = c.next();
					if (!(mustBe).equals(is)) {
						final String err = "Expected object " + mustBe + " differs from database object " + is;
						log.error(err);
						c.close();
						throw new Error(err);
					}
				} else {
					final String err = "Expected " + documents.size() + " rows in the '" + collectionName
							+ "' collection but was only " + c.numSeen();
					log.error(err);
					c.close();
					throw new Error(err);
				}
			}

			c.close();

		} catch (final IOException e) {
			throw new IllegalArgumentException(e);
		}

		return true;
	}

	/**
	 * Dumps content of given collection to the given file (in user.home), overwriting previous content. Typical usage
	 * is creation of "expected" data files.
	 */
	public void dump(final String collectionName, final String fileName) {

		// let the CollectionProducer inject the right collection
		final NamedCollectionQualifier q = new NamedCollectionQualifier(collectionName);
		final MongoCollection mc = imc.select(q).get();

		final StringBuilder readData = new StringBuilder();
		readData.append("{").append("\t\"").append(collectionName).append("\":\n\t[\n\t\t");

		final DBCursor c = mc.getDBCollection().find();
		while (c.hasNext()) {

			if (c.numSeen() > 0)
				readData.append(",\n\t\t");

			final DBObject document = c.next();
			JSON.serialize(document, readData);
		}
		readData.append("\n\t]\n}");

		final File f = new File(System.getProperty("user.home") + "/" + testingClass.getName().replace('.', '/') + "#"
				+ fileName + "-expected.json");
		log.debug("Dumping '{}' to '{}'", mc, f);

		try (
				final BufferedWriter w = new BufferedWriter(new FileWriter(f, f.getParentFile().mkdirs() & f
						.createNewFile()))
		) {
			w.write(readData.toString());
		} catch (final IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public enum Strategy {

		/** Add data to a table/collection */
		ADD,

		/** Set the data in a table/collection to the specified state */
		SET,

		/** Remove all data from a table/collection */
		TRIM
	}

	class NamedCollectionQualifier extends AnnotationLiteral<NamedCollection> implements NamedCollection {

		private static final long serialVersionUID = 1L;

		private final String collectionName;

		NamedCollectionQualifier(final String collectionName) {
			this.collectionName = collectionName;
		}

		@Override
		public String logicalName() {
			return collectionName;
		}
	}
}