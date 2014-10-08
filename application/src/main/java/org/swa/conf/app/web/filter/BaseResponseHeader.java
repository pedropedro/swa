package org.swa.conf.app.web.filter;


import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public abstract class BaseResponseHeader implements javax.servlet.Filter {

	/** get different flavours of a response header (for different browsers) */
	protected abstract IdentityHashMap<String, String> getHeaders();

	@Override
	public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain
			filterChain) throws IOException, ServletException {

		final HttpServletResponse rs = (HttpServletResponse) servletResponse;

		for (final Map.Entry<String, String> e : getHeaders().entrySet()) rs.addHeader(e.getKey(), e.getValue());

		filterChain.doFilter(servletRequest, rs);
	}

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}
}