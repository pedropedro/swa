package org.swa.conf.mongo.collections;

import org.bson.types.ObjectId;
import org.swa.conf.datatypes.User;
import org.swa.conf.mongo.annotations.DocumentAttributes;
import org.swa.conf.mongo.annotations.DocumentCriticality;

@DocumentAttributes(criticality = DocumentCriticality.NORMAL)
public class UserCollection extends User {

	private static final long	serialVersionUID	= 1L;

	public UserCollection withOid() {
		super.setId(new ObjectId());
		return this;
	}

	public UserCollection withOid(final String s) {
		super.setId(new ObjectId(s));
		return this;
	}

	@Override
	public ObjectId getId() {
		return (ObjectId) super.getId();
	}
}