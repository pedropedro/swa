package org.swa.conf.monitoring;

import java.util.Random;

import javax.ejb.Remote;

@Remote
public interface MonitoredService {

	public Random test(final Random r);
}