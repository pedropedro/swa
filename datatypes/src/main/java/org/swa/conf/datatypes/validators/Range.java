package org.swa.conf.datatypes.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

/** Numeric value in a range < min ; max > */
@Constraint(validatedBy = {Range.RangeValidator.class})
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR,
		ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Range {

	String message() default "{invalid.range}";

	String min() default "";

	String max() default "";

	/**
	 * Identification of an object guarded by this constraint, example: "parameter X" - this text will be used by
	 * building the error message, for example: "value of the parameter X must be within range {min} to {max}"
	 */
	String context() default "";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	class RangeValidator implements ConstraintValidator<Range, Number> {

		private BigDecimal min;
		private BigDecimal max;
		private boolean openInterval;

		private static final BigDecimal MIN = BigDecimal.valueOf(-Double.MAX_VALUE);
		private static final BigDecimal MAX = BigDecimal.valueOf(Double.MAX_VALUE);

		@Override
		public void initialize(final Range r) {
			min = r.min().isEmpty() ? MIN : new BigDecimal(r.min()).stripTrailingZeros();
			max = r.max().isEmpty() ? MAX : new BigDecimal(r.max()).stripTrailingZeros();
			openInterval = r.min().isEmpty() && r.max().isEmpty();
		}

		@Override
		public boolean isValid(final Number n, final ConstraintValidatorContext ctx) {

			if (n == null)
				return true;

			if (openInterval)
				return true;

			final BigDecimal N;

			if (n instanceof BigDecimal)
				N = (BigDecimal) n;
			else if (n instanceof BigInteger)
				N = new BigDecimal((BigInteger) n);
			else if (n instanceof Byte)
				N = new BigDecimal((Byte) n);
			else if (n instanceof Double)
				N = BigDecimal.valueOf((Double) n);
			else if (n instanceof Float)
				N = BigDecimal.valueOf((Float) n);
			else if (n instanceof Integer)
				N = BigDecimal.valueOf((Integer) n);
			else if (n instanceof Long)
				N = BigDecimal.valueOf((Long) n);
			else if (n instanceof Short)
				N = BigDecimal.valueOf((Short) n);
			else {
				ctx.disableDefaultConstraintViolation();
				ctx.buildConstraintViolationWithTemplate("Unexpected target " + n.getClass()).addConstraintViolation();
				return false;
			}

			return N.compareTo(min) >= 0 && N.compareTo(max) <= 0;
		}
	}
}