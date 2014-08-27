package org.swa.conf.mongo.collections;

import org.bson.types.ObjectId;
import org.swa.conf.datatypes.Speaker;
import org.swa.conf.mongo.annotations.DocumentAttributes;
import org.swa.conf.mongo.annotations.DocumentCriticality;

@DocumentAttributes(criticality = DocumentCriticality.NORMAL)
public class SpeakerCollection extends Speaker {

	private static final long	serialVersionUID	= 1L;

	public SpeakerCollection withOid() {
		super.setId(new ObjectId());
		return this;
	}

	public SpeakerCollection withOid(final String s) {
		super.setId(new ObjectId(s));
		return this;
	}

	@Override
	public ObjectId getId() {
		return (ObjectId) super.getId();
	}
}