package com.github.omribromberg.elasticsearch.datemath.formatter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.*;

public class DateMathFormatter {
    public static Collection<String> getAllPatternsBetween(ZonedDateTime start, ZonedDateTime end, String pattern) {
        final Collection<String> formattedPatterns = new ArrayList<>();
        final TemporalUnit lowestUnit = getLowestField(pattern).getTemporal().getBaseUnit();
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        for (int i = 0; i < lowestUnit.between(start, end); i++) {
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

}
