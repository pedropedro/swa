package org.swa.conf.monitoring;

import java.util.Date;
import javax.ejb.Remote;

import org.swa.conf.monitoring.StatisticsPersister.HistogramEntry;

@Remote
public interface MonitoringResourceMXBean {

	@DisplayName("Get histogram data")
	HistogramEntry[] getHistogram(

			@DisplayName("Statistics type: INVOCATIONS,EXCEPTIONS,RESPONSES") String statType,

			@DisplayName("Regex for filtering, .* for all") String key,

			@DisplayName("Time window from ...") Date from,

			@DisplayName("... time window to") Date to,

			@DisplayName("How coarse to aggregate ? Format: <integer>[D|H|M] (for Day, Hour, " +
					"Minute)") String coarseness);

}