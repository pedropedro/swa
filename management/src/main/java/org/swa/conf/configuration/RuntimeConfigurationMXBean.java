package org.swa.conf.configuration;

import javax.ejb.Remote;

@Remote
public interface RuntimeConfigurationMXBean {

	void setPasswordChangePeriodInDays(Integer days);

	Integer getPasswordChangePeriodInDays();

}