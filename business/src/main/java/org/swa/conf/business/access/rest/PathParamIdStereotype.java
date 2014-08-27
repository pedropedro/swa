package org.swa.conf.business.access.rest;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.inject.Stereotype;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

// import javax.ws.rs.PathParam;

@Stereotype
// @PathParam("id")
@NotNull
@Pattern(regexp = "[0-9a-fA-F]*", message = "{invalid.id}")
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface PathParamIdStereotype {

	// It's a pity - we can't aggregate @PathParam along with its validators into one single annotation :-/
	// So just aggregate the validators @see PathParamIdValidator

}