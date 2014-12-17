package org.swa.conf.app.web;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.json.JsonArray;
import javax.json.JsonObject;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AppTest extends AngularTestUtil {

	@BeforeClass
	public static void setup1() throws Exception {
		loadInvariants("myApp",
				"lib/angularjs/angular-touch.min.js",
				"lib/angularjs/angular-animate.min.js",
				"lib/angularjs/angular-resource.min.js",
				"lib/angularjs/angular-route.min.js",
				"lib/angular-ui/ui-bootstrap.js",
				"lib/angular-ui/ui-grid.js",
				"lib/angular-ui/ui-router.js",
				"lib/date-format.js"
		);
	}

	@Before
	public void setupN() throws Exception {
		loadApp("script/myApp.js");
	}

	@Test
	public void serviceTest() {

		boot();

		// myService without configuration gets injected the real myDiFactory and myDiService objects:
		// f5() := n*n,  f4() := 'C' + ( <param> + 21 )
		Assert.assertEquals(9.0, $inj("myService").callMember("f5", 3));
		Assert.assertEquals("CS21", $inj("myService").callMember("f4", "S"));
		// Javascript is quite dynamic ...
		Assert.assertEquals("C0", $inj("myService").callMember("f4", -21));
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
		Assert.assertEquals(27.0, $inj("myService").callMember("f5", 3));
		Assert.assertEquals("ZS21", $inj("myService").callMember("f4", "S"));
		Assert.assertEquals("Z0", $inj("myService").callMember("f4", -21));
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
		Assert.assertEquals(27.0, $inj("myService").callMember("f5", 3));
		Assert.assertEquals("ZS", $inj("myService").callMember("f4", "S"));
		Assert.assertEquals("Z-21", $inj("myService").callMember("f4", -21));
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
			$inj("myAlert").call(null, "Alert!");
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

		Assert.assertEquals("42", $inj("myAlert").call(null, "Alert!"));
	}

	@Test
	public void factoryDependsOnForeignModuleTest() {

		boot();

		// instead of hard coding constants in our test, get a reference to the providing service ...
		final Map<String, Integer> c = (Map<String, Integer>) $inj("uiGridConstants").get("filter");

		// uiGrid = { columns : [ <column> ] }
		// column = { name : <string>, filters : [ <filter> ] }
		// filter = { term : <string>, condition : <int> }
		// Filter constants = [ "STARTS_WITH","ENDS_WITH","EXACT","CONTAINS","GREATER_THAN","GREATER_THAN_OR_EQUAL",
		//						"LESS_THAN","LESS_THAN_OR_EQUAL","NOT_EQUAL"]

		// no filter set
		Assert.assertEquals("", $inj("rsql").callMember("getWhere", json(json().add("columns", jArr()).build())));

		// test every possible filter combination
		final JsonObject uiGrid = json().add("columns", jArr()

				.add(json().add("name", "COL1").add("filters", jArr()
						.add(json().add("term", "EQ").add("condition", c.get("EXACT")))))

				.add(json().add("name", "COL2").add("filters", jArr()
						.add(json().add("term", "SW").add("condition", c.get("STARTS_WITH")))
						.add(json().add("term", "EW").add("condition", c.get("ENDS_WITH")))
						.add(json().add("term", "CT").add("condition", c.get("CONTAINS")))
						.add(json().add("term", "NE").add("condition", c.get("NOT_EQUAL")))))

				.add(json().add("name", "COL3").add("filters", jArr()
						.add(json().add("term", "0").add("condition", c.get("GREATER_THAN")))
						.add(json().add("term", "10").add("condition", c.get("LESS_THAN")))))

				.add(json().add("name", "COL4").add("filters", jArr()
						.add(json().add("term", "9").add("condition", c.get("LESS_THAN_OR_EQUAL")))
						.add(json().add("term", "0").add("condition", c.get("GREATER_THAN_OR_EQUAL")))))).build();


		Assert.assertEquals("COL1=='^EQ' and COL2=='^SW*' and COL2=='*EW' and COL2=='*CT*' and COL2!='NE' and " +
						"COL3>'0' and COL3<'10' and COL4<='9' and COL4>='0'",
				$inj("rsql").callMember("getWhere", json(uiGrid)));

//		System.out.println("break point");
	}

	@Test
	public void directiveTest() {

		boot();

		final ScriptObjectMirror o1 = (ScriptObjectMirror) $inj("$compile").call(null, "<s-dir></s-dir>");

		final ScriptObjectMirror o2 = (ScriptObjectMirror) o1.call(null, getScopeMock());

		$inj("$rootScope").callMember("$digest");

		final Object o3 = o2.get("html"); //o2.callMember("html");

		Assert.assertEquals("<p>ABC1</p>", o3);
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
		final Object inputArray = json(jArr().add("Ian").add("Yo").add("Mark").build());
		final ScriptObjectMirror filteredArray = (ScriptObjectMirror) execFilter("filter", inputArray, "a");
		Assert.assertEquals(2, filteredArray.size());
		Assert.assertEquals("Ian", filteredArray.get("0"));
		Assert.assertEquals("Mark", filteredArray.get("1"));
	}

	/* ControllerEmitTest ControllerBroadcastTest
	  <button ng-click="$emit('MyEvent')">$emit('MyEvent')</button>
      <button ng-click="$broadcast('MyEvent')">$broadcast('MyEvent')</button>

	*/

	@Test
	public void controllerTest() {

		boot();

		// execute our Controller -> the $scope should get initialised
		execController("MainCtrl", getScopeMock());

		// just to test the logging in our App
		Assert.assertEquals("INFO logged", inspectScope("$log.info.logs.0.0"));
		// inspect $scope ...
		Assert.assertEquals(1, inspectScope("getTable().getPageParam()"));
		Assert.assertEquals(5, inspectScope("getTable().getRowsParam()"));
		Assert.assertNull(inspectScope("getTable().getSortParam()"));
		// no http request == no data to display
		Assert.assertEquals(EMPTY_JSON, inspectScope("getTable().data"));

		// prepare server mock response
		final JsonArray mockData = jArr()
				.add(json().add("name", "Name 1").add("description", "D1")
						.add("from", Instant.parse("2015-03-03T00:00:00Z").toEpochMilli())
						.add("to", Instant.parse("2015-03-06T00:00:00Z").toEpochMilli())
						.add("location", json().add("name", "Frankfurt")))
				.add(json().add("name", "Name 5").add("description", "D8")
						.add("from", Instant.parse("2015-07-12T00:00:00Z").toEpochMilli())
						.add("to", Instant.parse("2015-07-12T00:00:00Z").toEpochMilli())
						.add("location", json().add("name", "München"))).build();

		final HttpMock http = getMockHttp();
		http.expectGET("rest/conferences?p=1&r=5", HttpMock.IGNORE).callMember("respond", json(mockData));

		// get and run our query (send $resource request) and "wait" until server has responded
		inspectScope("getTable().query()");

		// simulate server response arrival
		http.flush(1, true);

		http.verifyNoOutstandingRequest();

//		final Object data = inspectScope("getTable().data");

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

//		System.out.println("break point");
	}

	@Test
	public void controllerWithServerErrorTest() {

		boot();

		// execute our Controller -> the $scope should get initialised
		execController("MainCtrl", getScopeMock());

		final HttpMock http = getMockHttp();
		http.expectGET("rest/conferences?p=1&r=5", HttpMock.IGNORE).callMember("respond", 500,
				json(json().add("Hehe", "java.sprache.NullPointerException at row 42").build()));

		inspectScope("getTable().query()");
		http.flush(1, true);
		http.verifyNoOutstandingRequest();

		Assert.assertEquals(EMPTY_JSON, inspectScope("getTable().data")); // no data received, just an exception
		Assert.assertEquals("java.sprache.NullPointerException at row 42\n", inspectScope("getTable().errors.0"));
		Assert.assertNull(inspectScope("getTable().errors.1"));

//		System.out.println("break point");
	}

}
