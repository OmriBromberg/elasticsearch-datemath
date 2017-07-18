package org.elasticsearchdatemath.parser;

public class DateMathParseException extends RuntimeException {
    public DateMathParseException(String message) {
        super(message);
    }

    public DateMathParseException(String message, Throwable e) {
        super(message);
        this.initCause(e);
    }
}
