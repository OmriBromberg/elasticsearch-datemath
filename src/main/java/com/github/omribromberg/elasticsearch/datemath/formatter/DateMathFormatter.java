package com.github.omribromberg.elasticsearch.datemath.formatter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.*;

public class DateMathFormatter {
    private final FieldSymbol lowestField;
    private final DateTimeFormatter formatter;

    public DateMathFormatter(String pattern) {
        lowestField = getLowestField(pattern);
        formatter = DateTimeFormatter.ofPattern(pattern);
    }

    public static Collection<String> getAllPatternsBetween(ZonedDateTime start, ZonedDateTime end, String pattern) {
        final TemporalUnit lowestUnit = getLowestField(pattern).getTemporal().getBaseUnit();
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        return getAllPatternsBetween(start, end, lowestUnit, formatter);
    }

    private static Collection<String> getAllPatternsBetween(ZonedDateTime start, ZonedDateTime end, TemporalUnit lowestUnit, DateTimeFormatter formatter) {
        final Collection<String> formattedPatterns = new ArrayList<>();

        for (int i = 0; i <= lowestUnit.between(start.truncatedTo(lowestUnit), end.truncatedTo(lowestUnit)); i++) {
            formattedPatterns.add(formatter.format(start.plus(i, lowestUnit)));
        }

        return formattedPatterns;
    }

    private static FieldSymbol getLowestField(String pattern) {
        final Set<FieldSymbol> fields = new HashSet<>();

        boolean escaped = false;
        FieldSymbol lowest = null;

        for (char current : pattern.toCharArray()) {
            if (current == '\'') {
                escaped = !escaped;
            } else if (!escaped) {
                final FieldSymbol symbol = FieldSymbol.of(current);
                if (Objects.nonNull(symbol)) {
                    fields.add(symbol);
                    if (lowest == null || lowest.ordinal() > symbol.ordinal()) {
                        lowest = symbol;
                    }
                }
            }
        }

        if (Objects.isNull(lowest)) {
            throw new DateMathFormatException("Pattern '" + pattern + "' does not contain any field symbols");
        }

        if (!lowest.checkDependencies(fields)) {
            throw new DateMathFormatException("Field '" + lowest.name() + "' does not have all of his dependencies");
        }

        return lowest;
    }

    public Collection<String> getAllPatternsBetween(ZonedDateTime start, ZonedDateTime end) {
        return getAllPatternsBetween(start, end, lowestField.getTemporal().getBaseUnit(), formatter);
    }

}
