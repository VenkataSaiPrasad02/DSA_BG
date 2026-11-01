package com.dsabg.customroom.validation;

import com.dsabg.customroom.entity.enums.ProblemCount;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ProblemCountValidator
        implements ConstraintValidator<ValidProblemCount, Integer> {

    @Override
    public boolean isValid(
            Integer value,
            ConstraintValidatorContext context) {

        // Let @NotNull handle null values.
        return value == null || ProblemCount.isValid(value);
    }
}