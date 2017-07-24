package com.github.omribromberg.elasticsearch.datemath.parser;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;


public class DateMathParser {
    private static final Map<Character, ChronoUnit> mathUnits = new HashMap() {{
        put('y', ChronoUnit.YEARS);
        put('M', ChronoUnit.MONTHS);
        put('w', ChronoUnit.WEEKS);
        put('d', ChronoUnit.DAYS);
        put('H', ChronoUnit.HOURS);
        put('h', ChronoUnit.HOURS);
        put('m', ChronoUnit.MINUTES);
        put('s', ChronoUnit.SECONDS);
    }};
    
    private final String pattern;
    private final ZoneId zone;
    private final Supplier<ZonedDateTime> nowSupplier;
    private final String nowPattern;
    private final DateTimeFormatter formatter;

    public DateMathParser(String pattern, ZoneId zone, Supplier<ZonedDateTime> nowSupplier, String nowPattern) {
        this.pattern = pattern;
        this.zone = zone;
        this.nowSupplier = nowSupplier;
        this.nowPattern = nowPattern;
        formatter = DateTimeFormatter
                .ofPattern(pattern)
                .withZone(zone);
    }

    public ZonedDateTime resolveExpression(String expression) {
        return expression.startsWith(nowPattern) ? resolveNowExpression(expression) : resolveDateTimeExpression(expression);
    }

    private ZonedDateTime parseMathExpression(String mathExpression, ZonedDateTime time) {
        for (int i = 0; i < mathExpression.length(); ) {
            final int sign;
            final boolean round;

            char current = mathExpression.charAt(i++);

            if (current == '/') {
                round = true;
                sign = 1;
            } else {
                round = false;
                if (current == '+') {
                    sign = 1;
                } else if (current == '-') {
                    sign = -1;
                } else {
                    throw new DateMathParseException("operator not supported for date math " + mathExpression);
                }
            }

            if (i >= mathExpression.length()) {
                throw new DateMathParseException("truncated date math " + mathExpression);
            }

            final int num;

            if (!Character.isDigit(mathExpression.charAt(i))) {
                num = 1;
            } else {
                int numFrom = i;

                while (i < mathExpression.length() && Character.isDigit(mathExpression.charAt(i))) {
                    i++;
                }

                if (i >= mathExpression.length()) {
                    throw new DateMathParseException("truncated date math " + mathExpression);
                }
                num = Integer.parseInt(mathExpression.substring(numFrom, i));

            }

            if (round) {
                if (num != 1) {
                    throw new DateMathParseException("rounding `/` can only be used on single unit types " + mathExpression);
                }
            }

            char unit = mathExpression.charAt(i++);
            ChronoUnit mathUnit = mathUnits.get(unit);

            if (Objects.isNull(mathUnit)) {
                throw new DateMathParseException("unit " + unit + " not supported for date math " + mathExpression);
            }

            time = round ? time.truncatedTo(mathUnit) : time.plus(sign * num, mathUnit);
        }
        return time;
    }

    private ZonedDateTime parseDateTimeExpression(String dateTimeExpression) {
        try {
            return getDateTimeWithDefaults(dateTimeExpression);
        } catch (IllegalArgumentException e) {
            throw new DateMathParseException("failed to parse date field " + dateTimeExpression + " with format " + pattern, e);
        }
    }

    private ZonedDateTime resolveDateTimeExpression(String dateTimeExpression) {
        int index = dateTimeExpression.indexOf("||");

        if (index == -1) {
            return parseDateTimeExpression(dateTimeExpression);
        }

        final ZonedDateTime time = parseDateTimeExpression(dateTimeExpression.substring(0, index));
        final String mathString = dateTimeExpression.substring(index + 2);

        return parseMathExpression(mathString, time);
    }

    private ZonedDateTime resolveNowExpression(String expression) {
        return parseMathExpression(expression.substring(nowPattern.length()), nowSupplier.get());
    }

    private ZonedDateTime getDateTimeWithDefaults(String dateTimeExpression) {
        TemporalAccessor parsed = formatter.parse(dateTimeExpression);
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
                .appendPattern(pattern);

        if (!parsed.isSupported(ChronoField.YEAR)) {
            builder = builder.parseDefaulting(ChronoField.YEAR, 1970);
        }
        if (!parsed.isSupported(ChronoField.MONTH_OF_YEAR)) {
            builder = builder.parseDefaulting(ChronoField.MONTH_OF_YEAR, 1);
        }
        if (!parsed.isSupported(ChronoField.DAY_OF_MONTH)) {
            builder = builder.parseDefaulting(ChronoField.DAY_OF_MONTH, 1);
        }
        if (!parsed.isSupported(ChronoField.HOUR_OF_DAY)) {
            builder = builder.parseDefaulting(ChronoField.HOUR_OF_DAY, 0);
        }
        if (!parsed.isSupported(ChronoField.MINUTE_OF_HOUR)) {
            builder = builder.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0);
        }
        if (!parsed.isSupported(ChronoField.INSTANT_SECONDS)) {
            builder = builder.parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0);
        }
        if (!parsed.isSupported(ChronoField.MILLI_OF_SECOND)) {
            builder = builder.parseDefaulting(ChronoField.MILLI_OF_SECOND, 0);
        }

        return ZonedDateTime.parse(dateTimeExpression, builder.toFormatter().withZone(zone));
    }
}
