package org.swa.conf.mongo.collections;

import org.swa.conf.datatypes.Location;
import org.swa.conf.mongo.annotations.DocumentAttributes;
import org.swa.conf.mongo.annotations.DocumentCriticality;

@DocumentAttributes(criticality = DocumentCriticality.HIGH)
public class LocationCollection extends Location {

	private static final long serialVersionUID = 1L;
}