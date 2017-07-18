package com.github.omribromberg.elasticsearch.datemath.parser;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.function.LongSupplier;


public class DateMathBuilder {
    private static final String defaultNowPattern = "now";
    private static final DateTimeZone defaultZone = DateTimeZone.UTC;
    private static final LongSupplier defaultNowSupplier = () -> DateTime.now().getMillis();
    private static final String defaultPattern = "YYYY.MM.dd";

    private final String pattern;
    private final DateTimeZone zone;
    private final LongSupplier nowSupplier;
    private final String nowPattern;

    public DateMathBuilder() {
        pattern = defaultPattern;
        zone = defaultZone;
        nowSupplier = defaultNowSupplier;
        nowPattern = defaultNowPattern;
    }

    private DateMathBuilder(String pattern, DateTimeZone zone, LongSupplier nowSupplier, String nowPattern) {
        this.pattern = pattern;
        this.zone = zone;
        this.nowSupplier = nowSupplier;
        this.nowPattern = nowPattern;
    }

    public DateMathBuilder pattern(String pattern) {
        return new DateMathBuilder(pattern, zone, nowSupplier, nowPattern);
    }

    public DateMathBuilder zone(DateTimeZone zone) {
        return new DateMathBuilder(pattern, zone, nowSupplier, nowPattern);
    }

    public DateMathBuilder now(LongSupplier nowSupplier) {
        return new DateMathBuilder(pattern, zone, nowSupplier, nowPattern);
    }

    public DateMathBuilder nowPattern(String nowPattern) {
        return new DateMathBuilder(pattern, zone, nowSupplier, nowPattern);
    }

    public DateMathParser build() {
        return new DateMathParser(pattern, zone, nowSupplier, nowPattern);
    }
}
