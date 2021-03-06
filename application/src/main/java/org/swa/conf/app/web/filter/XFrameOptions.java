package org.swa.conf.app.web.filter;


import java.util.IdentityHashMap;
import javax.servlet.annotation.WebFilter;

@WebFilter(urlPatterns = {"/*"})
public class XFrameOptions extends BaseResponseHeader {

	@Override
	protected IdentityHashMap<String, String> getHeaders() {
		final IdentityHashMap<String, String> m = new IdentityHashMap<>(1);
		m.put("X-Frame-Options", "DENY");
		return m;
	}
}