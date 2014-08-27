package org.swa.conf.monitoring;

import java.util.Random;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@StatisticsSource
@LocalBean
@Stateless
public class MonitoredBean implements MonitoredService {

	@Override
	public Random test(final Random r) {

		final int i = r.nextInt(10);

		// System.out.println("Random: " + i + " instance: " + this);

		if (i >>> 1 == 0)
			throw new RuntimeException("Expected exception thrown");

		// artificial delay
		try {
			Thread.sleep(i << 2);
		} catch (final InterruptedException e) {
			System.out.println("################### let me sleep, please ! ###############");
		}

		// r is stateful, we have to return it to the REMOTE clients !
		return r;
	}
}