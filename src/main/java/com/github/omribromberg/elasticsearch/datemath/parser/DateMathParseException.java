package com.github.omribromberg.elasticsearch.datemath.parser;

class DateMathParseException extends RuntimeException {
    DateMathParseException(String message) {
        super(message);
    }

    DateMathParseException(String message, Throwable e) {
        super(message, e);
    }
}
