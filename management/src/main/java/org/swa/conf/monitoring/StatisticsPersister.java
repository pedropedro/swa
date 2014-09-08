package org.swa.conf.monitoring;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.Date;

/** Implementations MUST be thread safe ! */
public interface StatisticsPersister {

	static class HistogramEntry implements Serializable {

		private static final long serialVersionUID = 1L;
		Date start, end;
		Long measure;

		@ConstructorProperties({"start", "end", "measure"})
		public HistogramEntry(final Date start, final Date end, final Long measure) {
			this.start = start;
			this.end = end;
			this.measure = measure;
		}

		public Date getStart() {
			return start;
		}

		public Date getEnd() {
			return end;
		}

		public Long getMeasure() {
			return measure;
		}
	}

	static class PersistentEntry {

		private final String key;
		private final Date timestamp;
		private final Long measure;

		public PersistentEntry(final String key, final Date timestamp, final Long measure) {
			this.key = key;
			this.timestamp = timestamp;
			this.measure = measure;
		}

		public Date getTimestamp() {
			return timestamp;
		}

		public Long getMeasure() {
			return measure;
		}

		public String getKey() {
			return key;
		}
	}

	enum StatType {
		EXCEPTIONS, INVOCATIONS, RESPONSES
	}

	void resetStatistics();

	void addInvocationCount(String key);

	void addResponseTime(String key, long millis);

	void addExceptionCount(String key);

	/**
	 * Get accumulated statistics with {@code statType} and given {@code coarseness} in a time window for a method
	 * {@link StatisticsSource} (regex).
	 * <p/>
	 * Format for {@code coarseness}: <b>{@code <integer><unit>}</b> where {@code unit} is 'D' (day), 'H' (hour), 'M'
	 * (minute).
	 * <p/>
	 * A {@code null} in any of the time window boundaries means 'open interval'.
	 */
	HistogramEntry[] getStatistics(StatType statType, String key, Date from, Date to, String coarseness);
}