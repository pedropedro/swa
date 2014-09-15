package org.swa.conf.mongo.services;

import cz.jirutka.rsql.parser.RSQLParser;
import org.junit.Test;

public class JongoRsqlVisitorTest {

	@Test
	public void queryTest() {

		final String query = "name==('Name 1') and from >= 2015-01-01 " +
				"or desc==Ab*ef and list=in=(red,blue,green) and person.x=out=(a,b)";

		System.out.println(new RSQLParser().parse(query).accept(new JongoRsqlVisitor()));
	}
}