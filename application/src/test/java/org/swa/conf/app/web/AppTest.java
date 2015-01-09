package org.swa.conf.app.web;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class AppTest extends AngularTestUtil {

	@BeforeClass
	public static void setup1() throws Exception {
		loadApp("myApp", "myIndex.html");
	}

	@Test
	public void serviceTest() {

		boot();

		// myService without configuration gets injected the real myDiFactory and myDiService objects:
		// f5() := n*n,  f4() := 'C' + ( <param> + 21 )
		Assert.assertEquals(9.0, find("myService").callMember("f5", 3));
		Assert.assertEquals("CS21", find("myService").callMember("f4", "S"));
		// Javascript is quite dynamic ...
		Assert.assertEquals("C0", find("myService").callMember("f4", -21));
	}

	@Test
	public void mockedDiFactoryTest() {

		mock("myDiFactory", new HashMap<String, Function<Object[], Object>>() {
			{
				put("f1", args -> "Z");                 //           f2(n) = n^3
				put("f2", args -> args == null ? null : StrictMath.pow((Integer) args[0], 3d));
			}
		});

		boot();

		// myService with configured mock for myDiFactory, but not myDiService:
		// f5() := n*n*n,  f4() := 'Z' + ( <param> + 21 )
		Assert.assertEquals(27.0, find("myService").callMember("f5", 3));
		Assert.assertEquals("ZS21", find("myService").callMember("f4", "S"));
		Assert.assertEquals("Z0", find("myService").callMember("f4", -21));
	}

	@Test
	public void mockedDiFactoryAndServiceTest() {

		mock("myDiFactory", new HashMap<String, Function<Object[], Object>>() {
			{
				put("f1", args -> "Z");                 //           f2(n) = n^3
				put("f2", args -> args == null ? null : StrictMath.pow((Integer) args[0], 3d));
			}
		});

		mock("myDiService", new HashMap<String, Function<Object[], Object>>() {
			{
				put("f3", args -> args == null ? null : args[0]);
			}
		});

		boot();

		// myService with configured mock for myDiFactory, but not myDiService:
		// f5() := n*n*n,  f4() := 'Z' + <param>
		Assert.assertEquals(27.0, find("myService").callMember("f5", 3));
		Assert.assertEquals("ZS", find("myService").callMember("f4", "S"));
		Assert.assertEquals("Z-21", find("myService").callMember("f4", -21));
	}

	@Test
	public void windowAlertTest() {

		mock("$window", new HashMap<String, Function<Object[], Object>>() {
			{
				put("alert", args -> {
					throw new Error((String) args[0]);
				});
				put("document", args -> null);
				put("navigator", args -> null);
				put("history", args -> null);
				put("requestAnimationFrame", args -> null);
				put("cancelAnimationFrame", args -> null);
			}
		});

		boot();

		try {
			exec(find("myAlert"), "Alert!");
		} catch (final Error e) {
			Assert.assertEquals("Alert!", e.getMessage());
		}
	}

	@Test
	public void windowTest() {

		mock("$window", new HashMap<String, Function<Object[], Object>>() {
			{
				put("alert", args -> args[0]);
				put("document", args -> null);
				put("navigator", args -> null);
				put("history", args -> null);
				put("requestAnimationFrame", args -> null);
				put("cancelAnimationFrame", args -> null);
			}
		});

		boot();

		Assert.assertEquals("42", exec(find("myAlert"), "Alert!"));
	}

	@Test
	public void factoryDependsOnForeignModuleTest() {

		boot();

		// instead of hard coding constants in our test, get a reference to the providing service ...
		final Map<String, Integer> c = (Map<String, Integer>) find("uiGridConstants").get("filter");

		// uiGrid = { columns : [ <column> ] }
		// column = { name : <string>, filters : [ <filter> ] }
		// filter = { term : <string>, condition : <int> }
		// Filter constants = [ "STARTS_WITH","ENDS_WITH","EXACT","CONTAINS","GREATER_THAN","GREATER_THAN_OR_EQUAL",
		//						"LESS_THAN","LESS_THAN_OR_EQUAL","NOT_EQUAL"]

		// no filter set
		Assert.assertEquals("", find("rsql").callMember("getWhere", toJS(jObj().add("columns", jArr()).build())));

		// test every possible filter combination
		final JsonObject uiGrid = jObj().add("columns", jArr()

				.add(jObj().add("name", "COL1").add("filters", jArr()
						.add(jObj().add("term", "EQ").add("condition", c.get("EXACT")))))

				.add(jObj().add("name", "COL2").add("filters", jArr()
						.add(jObj().add("term", "SW").add("condition", c.get("STARTS_WITH")))
						.add(jObj().add("term", "EW").add("condition", c.get("ENDS_WITH")))
						.add(jObj().add("term", "CT").add("condition", c.get("CONTAINS")))
						.add(jObj().add("term", "NE").add("condition", c.get("NOT_EQUAL")))))

				.add(jObj().add("name", "COL3").add("filters", jArr()
						.add(jObj().add("term", "0").add("condition", c.get("GREATER_THAN")))
						.add(jObj().add("term", "10").add("condition", c.get("LESS_THAN")))))

				.add(jObj().add("name", "COL4").add("filters", jArr()
						.add(jObj().add("term", "9").add("condition", c.get("LESS_THAN_OR_EQUAL")))
						.add(jObj().add("term", "0").add("condition", c.get("GREATER_THAN_OR_EQUAL")))))).build();


		Assert.assertEquals("COL1=='^EQ' and COL2=='^SW*' and COL2=='*EW' and COL2=='*CT*' and COL2!='NE' and " +
						"COL3>'0' and COL3<'10' and COL4<='9' and COL4>='0'",
				find("rsql").callMember("getWhere", toJS(uiGrid)));

//		System.out.println("break point");
	}

	@Test
	public void directiveWithTemplateTest() {

		boot();

		final String htmlTemplate = (String) findDirective("templateDir").get("template");

		Assert.assertEquals("<p>ABC42</p>", evalExpression(htmlTemplate, extend("y", 1, newJson("x", 41))));
		Assert.assertEquals("<p>ABCDE</p>", evalExpression(htmlTemplate, extend("y", "E", newJson("x", "D"))));
	}

	@Test
	public void directiveWithLinkTest() {

		boot();

		final Object linkFunction = findDirective("linkDir").get("link");

		exec(linkFunction, find("$rootScope"), newJson("element", "xxx"), newJson("attribute", "yyy"));

		Assert.assertEquals("xxxyyy", inspectScope("zzz"));

	}

	@Test
	public void filterTest() {

		boot();

		Assert.assertEquals("321cba", execFilter("reverse", "abc123"));

		Assert.assertEquals("321CBA", execFilter("reverse", "abc123", "upper"));
		Assert.assertEquals("321CBA", execFilter("reverse", "abc123", "^"));
		Assert.assertEquals("321CBA", execFilter("reverse", "aBC123", "lower"));

		// built-in currency filter
		Assert.assertEquals("myCurr 123.00", execFilter("currency", "123", "myCurr "));

		// built-in array filter
		final Object inputArray = toJS(jArr().add("Ian").add("Yo").add("Mark").build());

		final List<?> filteredArray = asList(execFilter("filter", inputArray, "a"));

		Assert.assertEquals(2, filteredArray.size());
		Assert.assertEquals("Ian", filteredArray.get(0));
		Assert.assertEquals("Mark", filteredArray.get(1));
	}

	@Test
	public void controllerTest() {

		boot();

		// execute our Controller -> the $scope should get initialised
		execController("MainCtrl", getScopeMock());

		// just to test the logging in our App
		Assert.assertEquals("INFO logged", inspectScope("$log.info.logs.0.0"));
		// inspect $scope ...
		Assert.assertNotNull(inspectScope("getTable()"));
		Assert.assertEquals(EMPTY_JSON, inspectScope("getTable().errors"));
		// no http request == no data to display
		Assert.assertEquals(EMPTY_JSON, inspectScope("getTable().data"));

		// prepare server mock response
		final JsonArray mockData = jArr()
				.add(jObj().add("name", "Name 1").add("description", "D1")
						.add("from", Instant.parse("2015-03-03T00:00:00Z").toEpochMilli())
						.add("to", Instant.parse("2015-03-06T00:00:00Z").toEpochMilli())
						.add("location", jObj().add("name", "Frankfurt")))
				.add(jObj().add("name", "Name 5").add("description", "D8")
						.add("from", Instant.parse("2015-07-12T00:00:00Z").toEpochMilli())
						.add("to", Instant.parse("2015-07-12T00:00:00Z").toEpochMilli())
						.add("location", jObj().add("name", "München"))).build();

		final HttpMock http = getMockHttp();
		http.expectGET("rest/conferences?p=1&r=5", HttpMock.IGNORE).callMember("respond", toJS(mockData));

		// get and run our query (send $resource request) and "wait" until server has responded
		inspectScope("getTable().query()");

		Assert.assertTrue((Boolean) inspectScope("getTable().queryRunning"));

		// simulate server response arrival
		http.flush(1, true);

		Assert.assertFalse((Boolean) inspectScope("getTable().queryRunning"));
		http.verifyNoOutstandingRequest();

		// A boolean flag injected by AngularJS into the server response
		Assert.assertTrue((Boolean) inspectScope("getTable().data.$resolved"));

		// first row
		Assert.assertEquals("Name 1", inspectScope("getTable().data.0.name"));
		// second row
		Assert.assertEquals("München", inspectScope("getTable().data.1.location.name"));
		// no more rows
		Assert.assertNull(inspectScope("getTable().data.2"));
		// no errors encountered
		Assert.assertEquals(EMPTY_JSON, inspectScope("getTable().errors"));
	}

	@Test
	public void controllerWithServerErrorTest() {

		boot();

		execController("MainCtrl", getScopeMock());

		final HttpMock http = getMockHttp();
		http.expectGET("rest/conferences?p=1&r=5", HttpMock.IGNORE).callMember("respond", 500,
				newJson("msg", "java.sprache.NullPointerException at row 42"));

		// get and run our query (send $resource request) and "wait" until server has responded
		inspectScope("getTable().query()");
		Assert.assertTrue((Boolean) inspectScope("getTable().queryRunning"));

		// simulate server response arrival
		http.flush(1, true);

		Assert.assertFalse((Boolean) inspectScope("getTable().queryRunning"));
		http.verifyNoOutstandingRequest();

		Assert.assertEquals(EMPTY_JSON, inspectScope("getTable().data")); // no data received, just an exception
		Assert.assertEquals("java.sprache.NullPointerException at row 42", inspectScope("getTable().errors.0"));
		Assert.assertNull(inspectScope("getTable().errors.1"));
	}

	@Test
	public void scopeInheritanceTest() {

		boot();

		final Object parentScope = find("$rootScope");

		execController("MainCtrl", getScopeMock(parentScope));
		Assert.assertEquals("A", inspectScope("table.scopeInheritance", parentScope));
		Assert.assertEquals("A", inspectScope("scopeInheritance", parentScope));

		// child controller $scope inherits from parent's $scope
		final Object childScope = cloneScope(parentScope);
		Assert.assertEquals(1.0, inspectScope("$countChildScopes()", parentScope));
		Assert.assertEquals(0.0, inspectScope("$countChildScopes()", childScope));

		execController("ChildCtrl_1", getScopeMock(childScope));
		Assert.assertEquals("A", inspectScope("table.scopeInheritance", childScope));
		Assert.assertEquals("A", inspectScope("scopeInheritance", childScope));

		// change parent scope -> the change must be propagated to the child
		final Map<String, Object> parentScopeMap = (Map<String, Object>) parentScope;
		((Map<String, Object>)parentScopeMap.get("table")).put("scopeInheritance", "X");
		parentScopeMap.put("scopeInheritance", "X");
		Assert.assertEquals("X", inspectScope("table.scopeInheritance", childScope));
		Assert.assertEquals("X", inspectScope("scopeInheritance", childScope));

		// change child scope -> the change must NOT be propagated to the parent for simple properties ONLY,
		// objects (complex properties) are referenced !!! (i.e. not participating in JS prototype inheritance)
		execController("ChildCtrl_2", getScopeMock(childScope));
		Assert.assertEquals("I", inspectScope("table.scopeInheritance", parentScope));
		Assert.assertEquals("X", inspectScope("scopeInheritance", parentScope));

		Assert.assertEquals("I", inspectScope("table.scopeInheritance", childScope));
		Assert.assertEquals("I", inspectScope("scopeInheritance", childScope));
	}

	@Test
	@Ignore
	public void TEST() {
		exec("function P(){ this.a='a'; this.b={'x':'x'}; }; var p = new P();" +
						"function Kind(){}; Kind.prototype=p;" +
						"var child=new Kind(); print('a a:',p.a,child.a);" +
						"p.a='a2'; print('a2 a2:',p.a,child.a);" +
						"child.a='a1'; print('a2 a1:',p.a,child.a);" +
						"print('x x:', p.b.x,child.b.x);" +
						"p.b.x='x2'; print('x2 x2:',p.b.x,child.b.x);" +
						"child.b.x='x1'; print('x1 x1:',p.b.x,child.b.x);" +
						""
		);
	}
}
	/* ControllerEmitTest ControllerBroadcastTest
	  <button ng-click="$emit('MyEvent')">$emit('MyEvent')</button>
      <button ng-click="$broadcast('MyEvent')">$broadcast('MyEvent')</button>

	*/

