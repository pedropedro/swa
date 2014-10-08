package org.swa.conf.app.web.filter;


import java.util.IdentityHashMap;
import javax.servlet.annotation.WebFilter;

@WebFilter(urlPatterns = {"/*"})
public class StrictTransportSecurity extends BaseResponseHeader {

	@Override
	protected IdentityHashMap<String, String> getHeaders() {
		final IdentityHashMap<String, String> m = new IdentityHashMap<>(1);
		m.put("Strict-Transport-Security", "max-age=99999999; includeSubDomains");
		return m;
	}
}