package org.swa.conf.monitoring;

import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

/**
 * Default implementation retrieves accumulated history only - the time window and coarseness parameter in the
 * {@link #getStatistics(org.swa.conf.monitoring.StatisticsPersister.StatType, String, Date, Date, String)
 * getStatistics()} will be silently ignored. Statistics read means: total value since last application start.
 */
@ApplicationScoped
public class DefaultStatisticsPersister implements StatisticsPersister {

	private LinkedBlockingDeque<PersistentEntry>	invocations;
	private LinkedBlockingDeque<PersistentEntry>	exceptions;
	private LinkedBlockingDeque<PersistentEntry>	responses;

	@PostConstruct
	private void init() {
		invocations = new LinkedBlockingDeque<>(10000);
		exceptions = new LinkedBlockingDeque<>(1000);
		responses = new LinkedBlockingDeque<>(10000);
	}

	private void write(final LinkedBlockingDeque<PersistentEntry> collection, final PersistentEntry e) {
		while (true) {
			if (!collection.offer(e))
				for (int i = 0; i < 10; i++)
					collection.pollFirst();
			else
				return;
		}
	}

	@Override
	public void resetStatistics() {
		invocations.clear();
		exceptions.clear();
		responses.clear();
	}

	@Override
	public void addInvocationCount(final String key) {
		write(invocations, new PersistentEntry(key, new Date(), Long.valueOf(1)));

	}

	@Override
	public void addResponseTime(final String key, final long millis) {
		write(responses, new PersistentEntry(key, new Date(), Long.valueOf(millis)));
	}

	@Override
	public void addExceptionCount(final String key) {
		write(exceptions, new PersistentEntry(key, new Date(), Long.valueOf(1)));
	}

	@Override
	public HistogramEntry[] getStatistics(final StatType statType, final String key, final Date from, final Date to,
			final String coarseness) {

		LinkedBlockingDeque<PersistentEntry> collection;

		switch (statType) {
		case EXCEPTIONS:
			collection = exceptions;
			break;
		case INVOCATIONS:
			collection = invocations;
			break;
		case RESPONSES:
			collection = responses;
			break;
		default:
			throw new IllegalArgumentException("Unknown StatType" + statType);
		}

		final HistogramEntry[] stats = new HistogramEntry[1];
		final HistogramEntry histogramEntry = new HistogramEntry(new Date(0), new Date(), 0l);
		stats[0] = histogramEntry;

		for (final PersistentEntry e : collection) {
			if (e.getKey().matches(key))
				histogramEntry.measure += e.getMeasure();
		}

		return stats;
	}
}