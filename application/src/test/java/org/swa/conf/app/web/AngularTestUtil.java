package org.swa.conf.app.web;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.junit.After;
import org.junit.Before;

class AngularTestUtil {

	static final ScriptEngine E = new ScriptEngineManager().getEngineByName("nashorn");
	static final Invocable I = (Invocable) E;
	static final JsonObject EMPTY_JSON = Json.createObjectBuilder().build();
	static String APP_MODULE;
	static UnaryOperator<String> TEST_MODULE = s -> s + "$Test$";
	// MAVEN's <project>/webapp folder
	static String WebAppDir;

	@Before
	public void commonSetupBeforeEach() {
		// runs before @Before in the test
		// call(E.get("$W"), "beforeEachFnc");
	}

	@After
	public void commonTearDownAfterEach() {
		// runs after @After in the test
		// call(E.get("$W"), "afterEachFnc");
	}

	/** Load AngularJS and the App to be tested, may be from a @BeforeClass method */
	static void loadInvariants(final String appName, final String... scripts) throws Exception {

		APP_MODULE = appName;

		final Class THIS = AngularTestUtil.class;

		// MAVEN puts compiled test classes under <project>/target/.... directory
		WebAppDir = THIS.getResource("").getPath().split("target")[0] + "src/main/webapp/";

		// first, load browser object mocks and store them
		E.put("$W", exec(new String(Files.readAllBytes(Paths.get(THIS.getResource("/angular-headless.js").toURI())))));

		// then load AngularJS from local storage (no dependency to (inter)net for our JUnit tests ...)
		E.eval(new FileReader(WebAppDir + "lib/angularjs/angular.js"));

		// in a real browser, window.X is a global variable X. Fake it here ...
		exec("var angular=window.angular");

		// load AngularJS mocks
		exec(new String(Files.readAllBytes(Paths.get(THIS.getResource("/angular-mocks.js").toURI()))));

		// load all script dependencies
		for (final String s : scripts) E.eval(new FileReader(WebAppDir + s));
	}

	/** Reload the App to be tested, may be from a @Before method */
	void loadApp(final String... scripts) throws Exception {
		// load all script dependencies
		for (final String s : scripts) E.eval(new FileReader(WebAppDir + s));
		// let depend the tested App on ngMock (using a new dummy module)
		exec("angular.module('" + TEST_MODULE.apply(APP_MODULE) + "', ['ngMock','" + APP_MODULE + "']);");
	}

	/** Bootstrap the App */
	void boot() {
		// boot the App-Wrapper and store the main $injector
		E.put("$INJ", exec("angular.bootstrap(document, ['" + TEST_MODULE.apply(APP_MODULE) + "']);"));
	}

	/** Execute JavaScript */
	static Object exec(final String script) {
		try {
			return E.eval(script);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	/** Call a method in a previously executed JavaScript */
	static Object call(final Object ctx, final String method, final Object... params) {
		try {
			return I.invokeMethod(ctx, method, params);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	/** Get an injectable AngularJS object */
	ScriptObjectMirror $inj(final Object... params) {
		return (ScriptObjectMirror) call(E.get("$INJ"), "get", params);
	}

	/** Create new JS-Object with given Property */
	Object create(final String propertyName, final Object propertyObj) {
		return extend(propertyName, propertyObj, null);
	}

	/** Extend existing JS-Object with given Property */
	Object extend(final String propertyName, final Object propertyObj, final Object destination) {
		return call(E.get("$W"), "addProperty", propertyName, propertyObj, destination);
	}

	/** Convert javax.json.JsonStructure to ScriptObjectMirror */
	Object json(final JsonStructure json) {
		return call(E.get("$W"), "json2js", json.toString());
	}

	/** Retrieve $rootScope's property value located at "p1.p2.m1().p4.0.#!Z()" for example. */
	Object inspectScope(final String propertyChain) {
		Object rs = $inj("$rootScope");
		for (final String s : Arrays.asList(propertyChain.split("\\."))) {
			if (rs == null) break;
			if (s.endsWith("()")) rs = ((ScriptObjectMirror) rs).callMember(s.substring(0, s.length() - 2));
			else rs = ((Map<String, Object>) rs).get(s);
		}
		return rs;
	}

	/** JS-Object with mocked $scope (from ngMock) */
	Object getScopeMock() {
		return create("$scope", $inj("$rootScope"));
	}

	HttpMock getMockHttp() {
		return I.getInterface($inj("$httpBackend"), HttpMock.class);
	}

	/** Execute a controller */
	Object execController(final String name, final Object dependencies) {
		return $inj("$controller").call(null, name, dependencies);
	}

	/** Add function mocks to an Angular object (service, factory, constant, provider, ...)*/
	void mock(final String mockName, final Map<String, Function<Object[], Object>> functions) {

		E.put("_$Mock", new AbstractJSObject() {
			@Override
			public Object getMember(final String name) {
				final Function<Object[], Object> f = functions.get(name);
				if (f == null)
					throw new Error("Unknown function " + name + " in mock " + mockName);

				return new AbstractJSObject() {
					@Override
					public Object call(final Object thiz, final Object... args) {
						return f.apply(args);
					}
				};
			}
		});
		exec("angular.module('" + TEST_MODULE.apply(APP_MODULE) + "').constant('" + mockName + "',_$Mock);");
	}


	/** Get json object builder and save 20 characters on the screen ;-) */
	JsonObjectBuilder json() {
		return Json.createObjectBuilder();
	}

	/** Get json array builder and save 19 characters on the screen ;-) */
	JsonArrayBuilder jArr() {
		return Json.createArrayBuilder();
	}

	public interface HttpMock {

		/** Usage: expectGET("my/URL", IGNORE) ==> ignore HTTP Headers in the assertion */
		final Object IGNORE = exec("function(a){return true;}");

		ScriptObjectMirror when(String method, String url, String data, Object headers);

		ScriptObjectMirror whenGET(String url, Object headers);

		ScriptObjectMirror whenDELETE(String url, Object headers);

		ScriptObjectMirror whenJSONP(String url, Object headers);

		ScriptObjectMirror whenHEAD(String url, Object headers);

		ScriptObjectMirror whenPUT(String url, String data, Object headers);

		ScriptObjectMirror whenPOST(String url, String data, Object headers);

		ScriptObjectMirror whenPATCH(String url, String data, Object headers);

		ScriptObjectMirror expect(String method, String url, String data, Object headers);

		ScriptObjectMirror expectGET(String url, Object headers);

		ScriptObjectMirror expectDELETE(String url, Object headers);

		ScriptObjectMirror expectJSONP(String url, Object headers);

		ScriptObjectMirror expectHEAD(String url, Object headers);

		ScriptObjectMirror expectPUT(String url, Object data, Object headers);

		ScriptObjectMirror expectPOST(String url, Object data, Object headers);

		ScriptObjectMirror expectPATCH(String url, Object data, Object headers);

		void flush(Integer flushCount, Boolean doDigest);

		void verifyNoOutstandingExpectation(Boolean doDigest);

		void verifyNoOutstandingRequest();

		void resetExpectations();
	}
}