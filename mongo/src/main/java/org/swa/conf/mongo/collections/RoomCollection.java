package org.swa.conf.mongo.collections;

import org.bson.types.ObjectId;
import org.swa.conf.datatypes.Room;
import org.swa.conf.mongo.annotations.DocumentAttributes;
import org.swa.conf.mongo.annotations.DocumentCriticality;

@DocumentAttributes(criticality = DocumentCriticality.HIGH)
public class RoomCollection extends Room {

	private static final long	serialVersionUID	= 1L;

	public RoomCollection withOid() {
		super.setId(new ObjectId());
		return this;
	}

	public RoomCollection withOid(final String s) {
		super.setId(new ObjectId(s));
		return this;
	}

	@Override
	public ObjectId getId() {
		return (ObjectId) super.getId();
	}
}