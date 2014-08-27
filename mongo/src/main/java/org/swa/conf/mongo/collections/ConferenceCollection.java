package org.swa.conf.mongo.collections;

import org.bson.types.ObjectId;
import org.swa.conf.datatypes.Conference;
import org.swa.conf.mongo.annotations.DocumentAttributes;
import org.swa.conf.mongo.annotations.DocumentCriticality;

@DocumentAttributes(criticality = DocumentCriticality.NORMAL)
public class ConferenceCollection extends Conference {

	private static final long	serialVersionUID	= 1L;

	public ConferenceCollection withOid() {
		super.setId(new ObjectId());
		return this;
	}

	public ConferenceCollection withOid(final String s) {
		super.setId(new ObjectId(s));
		return this;
	}

	// @Override
	// public ObjectId getId() {
	// return (ObjectId) super.getId();
	// }
}