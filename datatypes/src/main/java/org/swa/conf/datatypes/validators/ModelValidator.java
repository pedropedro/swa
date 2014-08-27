package org.swa.conf.datatypes.validators;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

/**
 * Wrapper for a {@link javax.validation.Validator Validator} transforming its set of
 * {@link javax.validation.ConstraintViolation ConstraintViolations} to an Exception with aggregated violations
 * messages.
 */
@ApplicationScoped
public class ModelValidator {

	@Inject
	protected Validator	validator;

	public void validate(final Object o, final Class<?>... groups) throws ValidationException {

		final Set<ConstraintViolation<Object>> constraintViolations = validator.validate(o, groups);

		if (constraintViolations.isEmpty())
			return;

		final StringBuilder sb = new StringBuilder(512);
		sb.append("Constraint violation(s):\n");

		for (final ConstraintViolation<Object> cv : constraintViolations) {
			sb.append(cv.getRootBeanClass().getSimpleName().toLowerCase()).append('.');
			sb.append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append("\n");
			// System.out.println(cv);
			// System.out.println(cv.getExecutableParameters());
			// System.out.println(cv.getExecutableReturnValue());
			// System.out.println(cv.getInvalidValue());
			// System.out.println(cv.getLeafBean());
			// System.out.println(cv.getMessage());
			// System.out.println(cv.getPropertyPath());
			// System.out.println(cv.getRootBean());
			// System.out.println(cv.getRootBeanClass());
		}

		sb.deleteCharAt(sb.length() - 1);

		throw new ValidationException(sb.toString());
	}
}