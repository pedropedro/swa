package org.swa.conf.mongo.collections;

import org.swa.conf.datatypes.Talk;
import org.swa.conf.mongo.annotations.DocumentAttributes;
import org.swa.conf.mongo.annotations.DocumentCriticality;

@DocumentAttributes(criticality = DocumentCriticality.NORMAL)
public class TalkCollection extends Talk {

	private static final long serialVersionUID = 1L;
}