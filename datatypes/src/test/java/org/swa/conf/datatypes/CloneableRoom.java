package org.swa.conf.datatypes;

import java.util.concurrent.atomic.AtomicInteger;

import org.swa.conf.datatypes.validators.Range;

class CloneableRoom extends Room {

	private static final long	serialVersionUID	= 1L;

	private double						dbl;
	private AtomicInteger			ai;

	@Range(min = "1", max = "10.0", messageScrewed = true)
	double getDbl() {
		return dbl;
	}

	@Range(min = "1", max = "10.0")
	AtomicInteger getAi() {
		return ai;
	}

	CloneableRoom setDbl(final double dbl) {
		this.dbl = dbl;
		return this;
	}

	CloneableRoom setAi(final AtomicInteger ai) {
		this.ai = ai;
		return this;
	}

	@Override
	public Room clone() {
		final Room o = new Room();
		o.setId(getId());
		o.setName(getName());
		o.setCapacity(getCapacity());

		return o;
	}
}