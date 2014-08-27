package org.swa.conf.mongo.collections;

import org.bson.types.ObjectId;
import org.swa.conf.datatypes.Location;
import org.swa.conf.mongo.annotations.DocumentAttributes;
import org.swa.conf.mongo.annotations.DocumentCriticality;

@DocumentAttributes(criticality = DocumentCriticality.HIGH)
public class LocationCollection extends Location {

	private static final long	serialVersionUID	= 1L;

	public LocationCollection withOid() {
		super.setId(new ObjectId());
		return this;
	}

	public LocationCollection withOid(final String s) {
		super.setId(new ObjectId(s));
		return this;
	}

	@Override
	public ObjectId getId() {
		return (ObjectId) super.getId();
	}
}