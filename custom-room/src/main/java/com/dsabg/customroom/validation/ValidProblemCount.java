package com.dsabg.customroom.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ProblemCountValidator.class)
@Target({
        ElementType.FIELD,
        ElementType.PARAMETER,
        ElementType.RECORD_COMPONENT
})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidProblemCount {

    String message() default
            "Problem count must be one of: 1, 3, 5, 7, 9, 11, 13, 15";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}