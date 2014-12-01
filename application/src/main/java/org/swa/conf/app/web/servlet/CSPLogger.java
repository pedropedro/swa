package org.swa.conf.app.web.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(urlPatterns = {"/CSPLogger"})
public class CSPLogger extends HttpServlet {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private JsonWriterFactory jsonWriterFactory;

	@Override
	public void init() {
		final Map<String, Boolean> config = new HashMap<>();
		config.put(JsonGenerator.PRETTY_PRINTING, Boolean.TRUE);
		jsonWriterFactory = Json.createWriterFactory(config);
	}

	@Override
	protected void doPost(final HttpServletRequest rq, final HttpServletResponse rs) throws ServletException {

		final StringWriter stWriter = new StringWriter();

		try (JsonReader r = Json.createReader(new BufferedReader(new InputStreamReader(rq.getInputStream())));
			 JsonWriter w = jsonWriterFactory.createWriter(stWriter)
		) {

			w.write(r.read());

			log.info("\n{}", stWriter);

		} catch (final IOException ex) {
			log.error(ex.getMessage(), ex);
		}
	}
}