package org.swa.conf.app.web;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.Invocable;
import javax.script.ScriptEngine;

import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

class AngularTestUtil {

	static final ScriptEngine E = new NashornScriptEngineFactory().getScriptEngine(
			new String[]{"-Dnashorn.time", "--locale=de_DE", "--global-per-engine"});
	static final Invocable I = (Invocable) E;

	private static final boolean MS$ = System.getProperty("os.name").contains("Windows");
	private static String APP_MODULE;
	private static final String TEST_SUFFIX = "$Test$";

	/** Load browser mock and AngularJS from @BeforeClass method */
	static void loadApp(final String appName, final String mainHtmlPageName) throws Exception {

		APP_MODULE = appName;

		final Class THIS = AngularTestUtil.class;

		// MAVEN puts compiled test classes under <project>/target/.... directory
		final String projectDir = THIS.getResource("").getPath().split("target")[0];
		// MAVEN's <project>/webapp folder
		final String webAppDir = (MS$ ? projectDir.substring(1) : projectDir) + "src/main/webapp/";

		/** Env.js (env_nashorn.js) is too slow
		 // first, load env.js (browser mock) - @see http://www.envjs.com
		 E.eval(Files.newBufferedReader(Paths.get(THIS.getResource("/env_nashorn.js").toURI())));
		 // enable loading anonymous, inline and tagged (</script>) javascript code
		 exec("Envjs.scriptTypes['text/javascript']=true;    Envjs.scriptTypes['']=true;");
		 exec("window.name='NG_DEFER_BOOTSTRAP!';  window.location='file:///" + WebAppDir + mainHtmlPageName + "';");
		 */
		// first, load browser object mocks
		exec("   var window = { length : 0, location : {}, angular : {} };" +
				"var document = { " +
				" addEventListener : function(){}," +
				" createDocumentFragment : function(){ return{ appendChild:function(){ return { childNodes:[] };}," +
				" firstChild :{textContent:''} }; }," +
				" createElement : function(){ return{ pathname:'', setAttribute:function(){} }; }," +
				" querySelector : function(){}" +
				"};" +
				"var angular = window.angular;");

		// then load all scripts referenced in the tested app
		final Pattern scriptSrcPattern = Pattern.compile(".*src=\"(.*)\".*</script>.*");
		final Pattern scriptTagPattern = Pattern.compile("<script");
		for (final String line : Files.readAllLines(Paths.get(webAppDir + mainHtmlPageName))) {
			for (final String scriptUrl : scriptTagPattern.split(line)) {
				final Matcher m = scriptSrcPattern.matcher(scriptUrl);
				if (!m.matches()) continue;
				E.eval(Files.newBufferedReader(Paths.get(webAppDir + m.group(1))));
			}
		}

		// load AngularJS mocks
		E.eval(Files.newBufferedReader(Paths.get(THIS.getResource("/angular-mocks.js").toURI())));

	}

	void resetMocks(){
		// load test wrapper module with dependency on ngMock
		exec("angular.module('" + APP_MODULE + TEST_SUFFIX + "', ['ngMock','" + APP_MODULE + "']);");
	}

	/** Bootstrap the App */
	void boot() {
		// boot the App-Wrapper and save the main $injector
		E.put("$INJ", exec("angular.bootstrap(document, ['" + APP_MODULE + TEST_SUFFIX + "']);"));
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

	/** Find an AngularJS component - service, factory, filter, controller, ... */
	ScriptObjectMirror find(final Object... params) {
		return (ScriptObjectMirror) call(E.get("$INJ"), "get", params);
	}

	/** Retrieve property value located in given $scope (default $rootScope) at "p1.p2.m1().p4.0.#!Z()" for example. */
	@SuppressWarnings("unchecked")
	<R> R inspectScope(final Class<R> _, final String propertyChain, final Object... inspectedScope) {
		Object rs = inspectedScope.length == 0 ? find("$rootScope") : inspectedScope[0];
		for (final String s : Arrays.asList(propertyChain.split("\\."))) {
			if (rs == null) break;
			if (s.endsWith("()")) rs = ((ScriptObjectMirror) rs).callMember(s.substring(0, s.length() - 2));
			else rs = ((Map<String, Object>) rs).get(s);
		}
		return (R) rs;
	}

	/** JS-Object { $scope : SCOPE } with mocked AngularJS scope (default $rootScope) object */
	Object getScopeMock(final Object... usingAngularScope) {
		E.put("$$SCOPE$$", usingAngularScope.length == 0 ? find("$rootScope") : usingAngularScope[0]);
		return exec("(function(){return {'$scope': $$SCOPE$$};})()");
	}

	/** Clone given AngularJS $scope to a new child scope */
	ScriptObjectMirror cloneScope(final Object $scope) {
		return (ScriptObjectMirror) call($scope, "$new");
	}

	HttpMock getMockHttp() {
		return I.getInterface(find("$httpBackend"), HttpMock.class);
	}

	/** Execute a controller */
	Object execController(final String name, final Object dependencies) {
		return find("$controller").call(null, name, dependencies);
	}

	/** Execute a filter */
	Object execFilter(final String name, final Object... dependencies) {
		return ((JSObject) find("$filter").call(null, name)).call(null, dependencies);
	}

	/* Execute a nameless function */
	Object exec(final Object function, final Object... params) {
		return ((JSObject) function).call(null, params);
	}

	/* Evaluate an AngularJS {{expression}} using given parameters */
	Object evalExpression(final String expression, final Object paramsAsJson) {
		return ((JSObject) find("$interpolate").call(null, expression)).call(null, paramsAsJson);
	}

	/* Find a directive */
	ScriptObjectMirror findDirective(final String directiveName) {
		return (ScriptObjectMirror) find(directiveName + "Directive").get("0");
	}

	/** Add function mocks to an Angular object (service, factory, constant, provider, ...) */
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
		exec("angular.module('" + APP_MODULE + TEST_SUFFIX + "').value('" + mockName + "',_$Mock);");
	}

	/** Convert String to native JavaScript object */
	Object toJS(final String jsonStructure) {
		return exec("angular.fromJson('" + jsonStructure.replace("'", "\"") + "');");
	}

	/* Convert Javascript Array instance to java.util.List<?> */
	List<?> asList(final Object jsArray) {
		return ((ScriptObjectMirror) jsArray).to(List.class);
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