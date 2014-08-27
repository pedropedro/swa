package org.swa.conf.business.access.rest;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Constraint(validatedBy = {})
@NotNull
@Pattern(regexp = "[0-9a-fA-F]*")
@ReportAsSingleViolation
@Documented
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR,
	ElementType.PARAMETER, ElementType.LOCAL_VARIABLE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PathParamIdValidator {

	String message() default "{invalid.id}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}