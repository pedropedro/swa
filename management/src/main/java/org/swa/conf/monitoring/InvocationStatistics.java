package org.swa.conf.monitoring;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@StatisticsSource
@Interceptor
@Dependent
public class InvocationStatistics {

	@Inject
	private MonitoringResource monitor;

	@AroundInvoke
	public Object countMethodInvocation(final InvocationContext ctx) throws Exception {

		final String key = monitor.constructKey(ctx);

		try {
			return ctx.proceed();

		} catch (final Exception e) {
			monitor.exceptionThrown(key);
			if ("Expected exception thrown".equals(e.getMessage()))
				return ctx.getParameters()[0]; // just for tests !!!
			else
				throw e;

		} finally {
			monitor.methodInvoked(key);
		}
	}
}