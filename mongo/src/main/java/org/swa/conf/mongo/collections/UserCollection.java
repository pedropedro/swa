package org.swa.conf.mongo.collections;

import org.swa.conf.datatypes.User;
import org.swa.conf.mongo.annotations.DocumentAttributes;
import org.swa.conf.mongo.annotations.DocumentCriticality;

@DocumentAttributes(criticality = DocumentCriticality.NORMAL)
public class UserCollection extends User {

	private static final long serialVersionUID = 1L;
}