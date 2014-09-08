package org.swa.conf.datatypes.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/** Geographical latitude in range &lt-90.000000 ; +90.000000&gt */
@Constraint(validatedBy = {})
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR,
		ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.RUNTIME)
@Range(min = "-90.000000", max = "+90.000000")
public @interface GeoLatitude {

	/** the one from @Range will be used */
	String message() default "dummy";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}