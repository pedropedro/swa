package org.swa.conf.app.web.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cedarsoftware.util.io.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(urlPatterns = {"/CSPLogger"})
public class CSPLogger extends HttpServlet {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	protected void doPost(final HttpServletRequest rq, final HttpServletResponse rs) throws ServletException {

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(rq.getInputStream()))) {

			final StringBuilder rb = new StringBuilder();
			String s;
			while ((s = reader.readLine()) != null) rb.append(s);

			log.info("\n{}", JsonWriter.formatJson(rb.toString()));

		} catch (final IOException ex) {
			log.error(ex.getMessage(), ex);
		}
	}
}