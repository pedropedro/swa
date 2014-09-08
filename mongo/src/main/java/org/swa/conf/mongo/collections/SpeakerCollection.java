package org.swa.conf.mongo.collections;

import org.swa.conf.datatypes.Speaker;
import org.swa.conf.mongo.annotations.DocumentAttributes;
import org.swa.conf.mongo.annotations.DocumentCriticality;

@DocumentAttributes(criticality = DocumentCriticality.NORMAL)
public class SpeakerCollection extends Speaker {

	private static final long serialVersionUID = 1L;
}