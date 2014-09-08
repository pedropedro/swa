package org.swa.conf.mongo;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.shrinkwrap.spi.Identifiable;
import org.junit.Ignore;
import org.slf4j.Logger;

//TODO ablage für spätere wiederverwendung bei ASM byte code manipulation vom BasePersistenceBean.save interceptor
@Ignore
public class DatatypeEnhancer {

	@Inject
	private Logger log;

	private Map<String, List<Method>> embeddedDocuments;

	public <T> List<T> getFlattenSubdocuments(final T d) {

		final List<T> l = new ArrayList<>();
		l.add(d);

		final List<Method> getters = this.embeddedDocuments.get(d.getClass().getName());
		if (getters.isEmpty())
			return l;

		for (final Method getter : getters) {
			try {
				final Object embeddedDocument = getter.invoke(d);

				if (embeddedDocument instanceof Identifiable)
					l.addAll(this.getFlattenSubdocuments((T) embeddedDocument));

				else if (embeddedDocument instanceof List<?>) {
					for (final Object e : (List<?>) embeddedDocument) {
						l.addAll(this.getFlattenSubdocuments((T) e));
					}
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				this.log.error("Cannot invoke method {}.{}", d, getter.getName());
				throw new InternalError(e.getMessage());
			}
		}

		return l;
	}

	@PostConstruct
	public void processDatatypes() throws Exception {

		this.embeddedDocuments = new HashMap<>();

		final List<String> classNames = DatatypeEnhancer.getClasses(Identifiable.class.getPackage().getName()
				.replace('.', '/'));
		this.log.debug("Found documents {}", classNames);

		for (final String className : classNames) {

			final Class<?> clazz = Class.forName(className);

			if (Identifiable.class.isAssignableFrom(clazz)) {

				this.log.debug("Found document {}", className);

				final List<Method> docGetters = new ArrayList<>();

				for (final Method m : clazz.getMethods())
					if (Identifiable.class.isAssignableFrom(m.getReturnType())
							||

							(m.getGenericReturnType() instanceof ParameterizedType

									&& ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments().length
									== 1

									&& ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments()[0]
									instanceof Class

									&& Identifiable.class.isAssignableFrom((Class<?>) ((ParameterizedType) m
									.getGenericReturnType())
									.getActualTypeArguments()[0])

							)

							)
						docGetters.add(m);

				this.embeddedDocuments.put(className, docGetters);
			}
		}
	}

	/**
	 * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
	 *
	 * @param packageName
	 * 		The base package
	 * @return The classes
	 */
	public static List<String> getClasses(final String packageName) throws Exception {

		assert packageName != null;

		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;

		final String path = packageName.replace('.', '/');
		final Enumeration<URL> resources = classLoader.getResources(path);
		final List<String> dirs = new ArrayList<>();
		while (resources.hasMoreElements()) {
			final URL resource = resources.nextElement();
			final String fileName = resource.getFile();
			final String fileNameDecoded = URLDecoder.decode(fileName, "UTF-8");
			dirs.add(fileNameDecoded);
		}
		final ArrayList<String> classNames = new ArrayList<>();
		for (final String directory : dirs) {
			classNames.addAll(DatatypeEnhancer.findClasses(directory, packageName));
		}
		return classNames;
	}

	/**
	 * Recursive method used to find all class names in a given base directory and subdirs.
	 *
	 * @param directory
	 * 		The base directory
	 * @param packageName
	 * 		The package name for classes found inside the base directory
	 * @return The class names
	 * @throws Exception
	 */
	public static List<String> findClasses(final String directory, final String packageName) throws Exception {

		final List<String> classNames = new ArrayList<>();

		// JAR
		if (directory.startsWith("file:") && directory.contains("!")) {
			final String[] split = directory.split("!");
			final URL jar = new URL(split[0]);
			final ZipInputStream zip = new ZipInputStream(jar.openStream());
			ZipEntry entry = null;
			String className = null;
			while ((entry = zip.getNextEntry()) != null) {
				if ((className = entry.getName()).endsWith(".class")) {
					className = className.replaceAll("[$].*", "").replaceAll("[.]class", "").replace('/', '.');
					classNames.add(className);
				}
			}
			return classNames;
		}

		final File dir = new File(directory);
		if (!dir.exists())
			return Collections.emptyList();

		for (final File file : dir.listFiles()) {
			final String fileName = file.getName();
			if (file.isDirectory()) {
				assert !fileName.contains(".");
				classNames.addAll(DatatypeEnhancer.findClasses(file.getAbsolutePath(), packageName + "." + fileName));
			} else if (fileName.endsWith(".class") && !fileName.contains("$")) {
				classNames.add(packageName + '.' + fileName.substring(0, fileName.length() - 6));
			}
		}
		return classNames;
	}
}