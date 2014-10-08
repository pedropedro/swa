package org.swa.conf.app.web.filter;


import java.util.IdentityHashMap;
import javax.servlet.annotation.WebFilter;

@WebFilter(urlPatterns = {"/*"})
public class ContentSecurityPolicy extends BaseResponseHeader {

	@Override
	protected IdentityHashMap<String, String> getHeaders() {

		final IdentityHashMap<String, String> m = new IdentityHashMap<>(4);

		final String s = "report-uri CSPLogger; default-src 'self' data:; " +
				"script-src 'self' data: 'unsafe-eval' 'unsafe-inline'; style-src 'self' data: 'unsafe-inline';";

		m.put("Content-Security-Policy", s);
		m.put("X-Content-Security-Policy", s);
		m.put("X-WebKit-CSP", s);

		return m;
	}
}