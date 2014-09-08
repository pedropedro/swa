package org.swa.conf.business.access.rest;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.enterprise.inject.Stereotype;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

// import javax.ws.rs.PathParam;

@Stereotype
// @PathParam("id")
@NotNull
@Pattern(regexp = "[-0-9]*|[xX][0-9a-fA-F]*", message = "{invalid.id}")
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface PathParamIdStereotype {

	// It's a pity - we can't aggregate @PathParam along with its validators into one single annotation :-/
	// So just aggregate the validators @see PathParamIdValidator

}