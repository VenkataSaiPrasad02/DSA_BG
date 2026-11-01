package com.dsabg.problem.exception;

public class ProblemNotFoundException extends RuntimeException {
    public ProblemNotFoundException(String slug) {
        super("Could not find problem with Slug " + slug);
    }
}
