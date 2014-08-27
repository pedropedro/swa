package org.swa.conf.datatypes.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/** Geographical longitude in range &lt-180.000000 ; +180.000000&gt */
@Constraint(validatedBy = {})
@Documented
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR,
	ElementType.PARAMETER, ElementType.LOCAL_VARIABLE })
@Retention(RetentionPolicy.RUNTIME)
@Range(min = "-180.000000", max = "+180.000000")
public @interface GeoLongitude {

	/** the one from @Range will be used */
	String message() default "dummy";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}