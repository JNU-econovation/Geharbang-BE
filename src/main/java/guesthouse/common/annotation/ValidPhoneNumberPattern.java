package guesthouse.common.annotation;

import guesthouse.common.annotation.validator.ValidPhoneNumberPatternValidator;
import guesthouse.common.exception.message.ValidationMessage;
import jakarta.validation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = {ValidPhoneNumberPatternValidator.class})
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumberPattern {
    String message() default ValidationMessage.NOT_VALID_PHONE_NUMBER_PATTERN;
    Class[] groups() default {};
    Class[] payload() default {};
}
