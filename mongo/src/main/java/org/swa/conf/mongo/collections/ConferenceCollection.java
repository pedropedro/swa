package org.swa.conf.mongo.collections;

import org.swa.conf.datatypes.Conference;
import org.swa.conf.mongo.annotations.DocumentAttributes;
import org.swa.conf.mongo.annotations.DocumentCriticality;

@DocumentAttributes(criticality = DocumentCriticality.NORMAL)
public class ConferenceCollection extends Conference {

	private static final long serialVersionUID = 1L;
}