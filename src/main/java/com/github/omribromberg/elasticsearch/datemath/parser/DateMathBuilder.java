package com.github.omribromberg.elasticsearch.datemath.parser;


import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.function.Supplier;


public class DateMathBuilder {
    private static final String defaultNowPattern = "now";
    private static final ZoneId defaultZone = ZoneOffset.UTC;
    private static final Supplier<ZonedDateTime> defaultNowSupplier = () -> ZonedDateTime.now(defaultZone);
    private static final String defaultPattern = "YYYY.MM.dd";

    private final String pattern;
    private final ZoneId zone;
    private final Supplier<ZonedDateTime> nowSupplier;
    private final String nowPattern;

    public DateMathBuilder() {
        pattern = defaultPattern;
        zone = defaultZone;
        nowSupplier = defaultNowSupplier;
        nowPattern = defaultNowPattern;
    }

    private DateMathBuilder(String pattern, ZoneId zone, Supplier<ZonedDateTime> nowSupplier, String nowPattern) {
        this.pattern = pattern;
        this.zone = zone;
        this.nowSupplier = nowSupplier;
        this.nowPattern = nowPattern;
    }

    public DateMathBuilder pattern(String pattern) {
        return new DateMathBuilder(pattern, zone, nowSupplier, nowPattern);
    }

    public DateMathBuilder zone(ZoneId zone) {
        return new DateMathBuilder(pattern, zone, nowSupplier, nowPattern);
    }

    public DateMathBuilder now(Supplier<ZonedDateTime> nowSupplier) {
        return new DateMathBuilder(pattern, zone, nowSupplier, nowPattern);
    }

    public DateMathBuilder nowPattern(String nowPattern) {
        return new DateMathBuilder(pattern, zone, nowSupplier, nowPattern);
    }

    public DateMathParser build() {
        return new DateMathParser(pattern, zone, nowSupplier, nowPattern);
    }
}
