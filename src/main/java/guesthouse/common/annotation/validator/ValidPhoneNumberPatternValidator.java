package guesthouse.common.annotation.validator;


import guesthouse.common.annotation.ValidPhoneNumberPattern;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;


public class ValidPhoneNumberPatternValidator implements ConstraintValidator<ValidPhoneNumberPattern,String> {
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^010-?\\d{4}-?\\d{4}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return PHONE_NUMBER_PATTERN.matcher(value).matches();
    }
}
