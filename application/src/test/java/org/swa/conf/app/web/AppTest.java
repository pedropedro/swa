package org.swa.conf.app.web;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

// @FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppTest extends AngularTestUtil {

	@BeforeClass
	public static void setup1() throws Exception {
		loadApp("myApp", "myIndex.html", Browser.SIMPLE);
	}

	@Test
	public void serviceTest() {

		resetMocks();

		boot();

		// myService without configuration gets injected the real myDiFactory and myDiService objects:
		// f5(n) := n*n,  f4(p) := 'C' + ( p + 21 )
		Assert.assertEquals(9.0, find("myService").callMember("f5", 3));
		Assert.assertEquals("CS21", find("myService").callMember("f4", "S"));
		// Javascript is quite dynamic ...
		Assert.assertEquals("C0", find("myService").callMember("f4", -21));
	}

	@Test
	public void mockedDiFactoryTest() {

		resetMocks();

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

		resetMocks();

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

		resetMocks();

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

		resetMocks();

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

		resetMocks();

		boot();

		// instead of hard coding constants in our test, get a reference to the providing service ...
		final Map<String, Integer> c = (Map<String, Integer>) find("uiGridConstants").get("filter");

		// uiGrid = { columns : [ <column> ] }
		// column = { name : <string>, filters : [ <filter> ] }
		// filter = { term : <string>, condition : <int> }
		// Filter constants = [ "STARTS_WITH","ENDS_WITH","EXACT","CONTAINS","GREATER_THAN","GREATER_THAN_OR_EQUAL",
		//						"LESS_THAN","LESS_THAN_OR_EQUAL","NOT_EQUAL"]

		// no filter set
		Assert.assertEquals("", find("rsql").callMember("getWhere", toJS("{'columns':[]}")));

		// test every possible filter combination
		String uiGrid = "{'columns':[" +
				"{'name':'COL1','filters':[{'term':'EQ','condition':§EQ§}]}," +
				"{'name':'COL2','filters':[{'term':'SW','condition':§SW§},{'term':'EW','condition':§EW§}," +
				"                          {'term':'CT','condition':§CT§},{'term':'NE','condition':§NE§}]}," +
				"{'name':'COL3','filters':[{'term':'0', 'condition':§GT§},{'term':'10','condition':§LT§}]}," +
				"{'name':'COL4','filters':[{'term':'9','condition':§LTE§},{'term':'0','condition':§GTE§}]}" +
				"]}";
		uiGrid = uiGrid.replace("§EQ§", Integer.toString(c.get("EXACT")));
		uiGrid = uiGrid.replace("§SW§", Integer.toString(c.get("STARTS_WITH")));
		uiGrid = uiGrid.replace("§EW§", Integer.toString(c.get("ENDS_WITH")));
		uiGrid = uiGrid.replace("§CT§", Integer.toString(c.get("CONTAINS")));
		uiGrid = uiGrid.replace("§NE§", Integer.toString(c.get("NOT_EQUAL")));
		uiGrid = uiGrid.replace("§GT§", Integer.toString(c.get("GREATER_THAN")));
		uiGrid = uiGrid.replace("§LT§", Integer.toString(c.get("LESS_THAN")));
		uiGrid = uiGrid.replace("§LTE§", Integer.toString(c.get("LESS_THAN_OR_EQUAL")));
		uiGrid = uiGrid.replace("§GTE§", Integer.toString(c.get("GREATER_THAN_OR_EQUAL")));

		Assert.assertEquals("COL1=='^EQ' and COL2=='^SW*' and COL2=='*EW' and COL2=='*CT*' and COL2!='NE' and " +
						"COL3>'0' and COL3<'10' and COL4<='9' and COL4>='0'",
				find("rsql").callMember("getWhere", toJS(uiGrid)));
	}

	@Test
	public void directiveWithTemplateTest() {

		resetMocks();

		boot();

		final String htmlTemplate = (String) findDirective("templateDir").get("template");

		Assert.assertEquals("<p>ABC42</p>", evalExpression(htmlTemplate, toJS("{'y': 1 ,'x': 41}")));
		Assert.assertEquals("<p>ABCDE</p>", evalExpression(htmlTemplate, toJS("{'y':'E','x':'D'}")));
	}

	@Test
	public void directiveWithLinkTest() {

		resetMocks();

		boot();

		final Object linkFunction = findDirective("linkDir").get("link");

		exec(linkFunction, find("$rootScope"), toJS("{'element':'xxx'}"), toJS("{'attribute':'yyy'}"));

		Assert.assertEquals("xxxyyy", inspectScope(String.class, "zzz"));

	}

	@Test
	public void filterTest() {

		resetMocks();

		boot();

		Assert.assertEquals("321cba", execFilter("reverse", "abc123"));

		Assert.assertEquals("321CBA", execFilter("reverse", "abc123", "upper"));
		Assert.assertEquals("321CBA", execFilter("reverse", "abc123", "^"));
		Assert.assertEquals("321CBA", execFilter("reverse", "aBC123", "lower"));

		// built-in currency filter
		Assert.assertEquals("myCurr 123.00", execFilter("currency", "123", "myCurr "));

		// built-in array filter
		final List<?> filteredArray = asList(execFilter("filter", toJS("['Ian','Yo','Mark']"), "a"));

		Assert.assertEquals(2, filteredArray.size());
		Assert.assertEquals("Ian", filteredArray.get(0));
		Assert.assertEquals("Mark", filteredArray.get(1));
	}

	@Test
	public void controllerTest() {

		resetMocks();

		boot();

		// execute our Controller -> the $scope should get initialised
		execController("MainCtrl", getScopeMock());

		// just to test the logging in our App
		Assert.assertEquals("INFO logged", inspectScope(String.class, "$log.info.logs.0.0"));
		// inspect $scope ...
		Assert.assertNotNull(inspectScope(null, "getTable()"));
		Assert.assertTrue(inspectScope(Map.class, "getTable().errors").isEmpty());
		// no http request == no data to display
		Assert.assertTrue((inspectScope(Map.class, "getTable().data")).isEmpty());

		// prepare server mock response
		String mockData = "[" +
				"{'name':'Name 1','description':'D1','from':{0},'to':{1},'location':{'name':'Frankfurt'}}," +
				"{'name':'Name 5','description':'D8','from':{2},'to':{3},'location':{'name':'München'}}" +
				"]";
		mockData = mockData.replace("{0}", Long.toString(Instant.parse("2015-03-03T00:00:00Z").toEpochMilli()));
		mockData = mockData.replace("{1}", Long.toString(Instant.parse("2015-03-06T00:00:00Z").toEpochMilli()));
		mockData = mockData.replace("{2}", Long.toString(Instant.parse("2015-07-12T00:00:00Z").toEpochMilli()));
		mockData = mockData.replace("{3}", Long.toString(Instant.parse("2015-07-12T00:00:00Z").toEpochMilli()));

		final HttpMock http = getMockHttp();
		http.expectGET("rest/conferences?p=1&r=5", HttpMock.IGNORE).callMember("respond", toJS(mockData));

		// get and run our query (send $resource request) and "wait" until server has responded
		inspectScope(null, "getTable().query()");

		Assert.assertTrue(inspectScope(Boolean.class, "getTable().queryRunning"));

		// simulate server response arrival
		http.flush(1, true);

		Assert.assertFalse(inspectScope(Boolean.class, "getTable().queryRunning"));
		http.verifyNoOutstandingRequest();

		// A boolean flag injected by AngularJS into the server response
		Assert.assertTrue(inspectScope(Boolean.class, "getTable().data.$resolved"));

		// first row
		Assert.assertEquals("Name 1", inspectScope(String.class, "getTable().data.0.name"));
		// second row
		Assert.assertEquals("München", inspectScope(String.class, "getTable().data.1.location.name"));
		// no more rows
		Assert.assertNull(inspectScope(null, "getTable().data.2"));
		// no errors encountered
		Assert.assertTrue(inspectScope(Map.class, "getTable().errors").isEmpty());
	}

	@Test
	public void controllerWithServerErrorTest() {

		resetMocks();

		boot();

		execController("MainCtrl", getScopeMock());

		final HttpMock http = getMockHttp();
		http.expectGET("rest/conferences?p=1&r=5", HttpMock.IGNORE).callMember("respond", 500,
				toJS("{'msg':'java.sprache.NullPointerException at row 42'}"));

		// get and run our query (send $resource request) and "wait" until server has responded
		inspectScope(null, "getTable().query()");
		Assert.assertTrue(inspectScope(Boolean.class, "getTable().queryRunning"));

		// simulate server response arrival
		http.flush(1, true);

		Assert.assertFalse(inspectScope(Boolean.class, "getTable().queryRunning"));
		http.verifyNoOutstandingRequest();

		// no data received, just an exception
		Assert.assertTrue(inspectScope(Map.class, "getTable().data").isEmpty());
		Assert.assertEquals("java.sprache.NullPointerException at row 42",
				inspectScope(String.class, "getTable().errors.0"));
		Assert.assertNull(inspectScope(null, "getTable().errors.1"));
	}

	@Test
	public void scopeInheritanceTest() {

		resetMocks();

		boot();

		final Object parentScope = find("$rootScope");

		execController("MainCtrl", getScopeMock(parentScope));
		Assert.assertEquals("A", inspectScope(String.class, "table.scopeInheritance", parentScope));
		Assert.assertEquals("A", inspectScope(String.class, "scopeInheritance", parentScope));

		// child controller $scope inherits from parent's $scope
		final Object childScope = cloneScope(parentScope);
		Assert.assertEquals(1.0, inspectScope(Number.class, "$countChildScopes()", parentScope));
		Assert.assertEquals(0.0, inspectScope(Number.class, "$countChildScopes()", childScope));

		execController("ChildCtrl_1", getScopeMock(childScope));
		Assert.assertEquals("A", inspectScope(String.class, "table.scopeInheritance", childScope));
		Assert.assertEquals("A", inspectScope(String.class, "scopeInheritance", childScope));

		// a change in parent scope -> the change must be propagated to the child
		final Map<String, Object> parentScopeMap = (Map<String, Object>) parentScope;
		((Map<String, Object>) parentScopeMap.get("table")).put("scopeInheritance", "X");
		parentScopeMap.put("scopeInheritance", "X");
		Assert.assertEquals("X", inspectScope(String.class, "table.scopeInheritance", childScope));
		Assert.assertEquals("X", inspectScope(String.class, "scopeInheritance", childScope));

		// a change in child scope -> the change must NOT be propagated to the parent for simple properties ONLY,
		// objects (complex properties) are referenced !!! (i.e. not participating in JS prototype inheritance)
		execController("ChildCtrl_2", getScopeMock(childScope));
		Assert.assertEquals("I", inspectScope(String.class, "table.scopeInheritance", parentScope));
		Assert.assertEquals("X", inspectScope(String.class, "scopeInheritance", parentScope));

		Assert.assertEquals("I", inspectScope(String.class, "table.scopeInheritance", childScope));
		Assert.assertEquals("I", inspectScope(String.class, "scopeInheritance", childScope));
	}

	@Test
	public void eventEmitTest() {

		resetMocks();

		boot();

		// prepare a $scope hierarchy
		final Object $rs = find("$rootScope");
		final Object $cs1 = cloneScope($rs);
		final Object $cs2 = cloneScope($cs1);
		final Object $cs3 = cloneScope($cs2);
		final Object $cs4 = cloneScope($cs3);

		// collect listener test output
		final List<String> notificationChain = new ArrayList<>();
		E.put("NC", notificationChain);

		// let's $emit('MyEvent') from the $cs3:
		// - $cs4 may not be notified (is underneath the $cs3)
		// - $cs3(itself!), $cs2 and $cs1 must get notified
		// - $cs1 listener stops the event propagation, so
		// - $rs may not get notified

		// register listeners
		call($rs, "$on", "MyEvent", exec("function(e)   {throw new Error('$rootScope got notified !');}"));
		call($cs1, "$on", "MyEvent", exec("function(e)  {NC.add('$cs1'); e.stopPropagation()}"));
		call($cs2, "$on", "MyEvent", exec("function(e,p){NC.add('$cs2' + p['param1'])}"));
		call($cs3, "$on", "MyEvent", exec("function(e)  {NC.add('$cs3')}"));
		call($cs4, "$on", "MyEvent", exec("function(e)  {throw new Error('$cs4 got notified !');}"));

		// let's emit
		call($cs3, "$emit", "MyEvent", toJS("{'param1':'x'}"));

		Assert.assertEquals(3, notificationChain.size());
		Assert.assertEquals("$cs3", notificationChain.get(0));
		Assert.assertEquals("$cs2x", notificationChain.get(1));
		Assert.assertEquals("$cs1", notificationChain.get(2));
	}

	@Test
	public void eventBroadcastTest() {

		resetMocks();

		boot();

		// prepare a $scope hierarchy
		final Object $rs = find("$rootScope");
		final Object $cs1 = cloneScope($rs);
		final Object $cs2 = cloneScope($cs1);
		final Object $cs3 = cloneScope($cs2);
		final Object $cs4 = cloneScope($cs3);

		// collect listener test output
		final List<String> notificationChain = new ArrayList<>();
		E.put("NC", notificationChain);

		// let's $broadcast('MyEvent') from the $cs1:
		// - $rs may not be notified (is above the $cs1)
		// - $cs1(itself!) and all child scopes must get notified - propagation of a broadcast event cannot be stopped

		// register listeners
		call($rs, "$on", "MyEvent", exec("function(e)   {throw new Error('$rootScope got notified !');}"));
		call($cs1, "$on", "MyEvent", exec("function(e)  {NC.add('$cs1')}"));
		call($cs2, "$on", "MyEvent", exec("function(e)  {NC.add('$cs2')}"));
		call($cs3, "$on", "MyEvent", exec("function(e)  {NC.add('$cs3')}"));
		call($cs4, "$on", "MyEvent", exec("function(e,p){NC.add('$cs4' + p['param1'])}"));

		// let's emit
		call($cs1, "$broadcast", "MyEvent", toJS("{'param1':'y'}"));

		Assert.assertEquals(4, notificationChain.size());
		Assert.assertEquals("$cs1", notificationChain.get(0));
		Assert.assertEquals("$cs2", notificationChain.get(1));
		Assert.assertEquals("$cs3", notificationChain.get(2));
		Assert.assertEquals("$cs4y", notificationChain.get(3));
	}

	@Test
	public void decoratorTest() {

		resetMocks();

		boot();

		// myServiceToBeDecorated:  f5(n) := n*n,  f4(p) := 'C' + ( p + 21 )
		// the decorator: f5(n) := 7,     f4(p) := 'Augmented_' + f4_orig(p)
		Assert.assertEquals(7, find("myServiceToBeDecorated").callMember("f5", 3));
		Assert.assertEquals("Augmented_CS21", find("myServiceToBeDecorated").callMember("f4", "S"));
		// Javascript is quite dynamic ...
		Assert.assertEquals("Augmented_C0", find("myServiceToBeDecorated").callMember("f4", -21));

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

	public static void main(final String[] args) throws Exception {
		setup1();
		final AppTest a = new AppTest();
		for (int i = 0, letsTrainTheFan = 100000; i < letsTrainTheFan; i++) {
			a.serviceTest();
			a.mockedDiFactoryTest();
			a.mockedDiFactoryAndServiceTest();
			a.windowAlertTest();
			a.windowTest();
			a.factoryDependsOnForeignModuleTest();
			a.directiveWithTemplateTest();
			a.filterTest();
			a.controllerTest();
			a.controllerWithServerErrorTest();
			a.scopeInheritanceTest();
			a.eventEmitTest();
			a.eventBroadcastTest();
			a.decoratorTest();
		}
	}
}