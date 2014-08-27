package org.swa.conf.monitoring;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@StatisticsSource
@Interceptor
@Dependent
public class ResponseTimeStatistics {

	@Inject
	private MonitoringResource	monitor;

	@AroundInvoke
	public Object getResponseTime(final InvocationContext ctx) throws Exception {

		final long start = System.currentTimeMillis();
		final String key = monitor.constructKey(ctx);

		try {
			return ctx.proceed();

		} finally {
			final long duration = System.currentTimeMillis() - start;
			monitor.methodResponseTime(key, duration == 0 ? 1l : duration);
			// on my notebook with core i7 octo-core lasts a method call 0.8ms (long ... 0),
			// so after 1000 times called the statistics says: total time is 0ms :-)
		}
	}
}