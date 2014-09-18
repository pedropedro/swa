package org.swa.conf.business.access.rest;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Pattern;

@Constraint(validatedBy = {})
@Pattern(regexp = "([0-9a-zA-Z_]+[-+])*")
@ReportAsSingleViolation
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryParamSortByValidator {

	String message() default "{invalid.sortBy}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}