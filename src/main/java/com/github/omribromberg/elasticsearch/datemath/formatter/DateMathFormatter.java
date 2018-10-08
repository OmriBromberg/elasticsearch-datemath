package com.github.omribromberg.elasticsearch.datemath.formatter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.*;

public class DateMathFormatter {
    private final FieldSymbol lowestField;
    private final DateTimeFormatter formatter;

    public DateMathFormatter(String pattern) {
        lowestField = getLowestField(pattern);
        formatter = DateTimeFormatter.ofPattern(pattern);
    }

    public static Collection<String> getAllPatternsBetween(ZonedDateTime start, ZonedDateTime end, String pattern) {
        final FieldSymbol lowestField = getLowestField(pattern);
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        return getAllPatternsBetween(start, end, lowestField, formatter);
    }

    private static Collection<String> getAllPatternsBetween(ZonedDateTime start, ZonedDateTime end, FieldSymbol lowestField, DateTimeFormatter formatter) {
        final Collection<String> formattedPatterns = new ArrayList<>();
        final TemporalUnit lowestUnit = lowestField.getTemporal().getBaseUnit();

        for (int i = 0; i <= lowestUnit.between(getTruncated(start, lowestField), getTruncated(end, lowestField)); i++) {
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
            throw new DateMathFormatException(String.format("Pattern '%s' does not contain any field symbols", pattern));
        }

        if (!lowest.checkDependencies(fields)) {
            throw new DateMathFormatException(String.format("Field '%s' does not have all of his dependencies", lowest.name()));
        }

        return lowest;
    }

    private static ZonedDateTime getTruncated(ZonedDateTime dateTime, FieldSymbol field) {
        switch (field) {
            case G:
                dateTime = dateTime.with(ChronoField.YEAR_OF_ERA, ChronoField.YEAR_OF_ERA.rangeRefinedBy(dateTime).getMaximum());
                break;
            case Y:
            case y:
            case u:
                dateTime = dateTime.with(TemporalAdjusters.firstDayOfYear());
                break;
            case Q:
            case q:
                dateTime = dateTime.with(IsoFields.DAY_OF_QUARTER, IsoFields.DAY_OF_QUARTER.rangeRefinedBy(dateTime).getMinimum());
                break;
            case M:
            case L:
                dateTime = dateTime.with(TemporalAdjusters.firstDayOfMonth());
                break;
            case W:
            case w:
                dateTime = dateTime.with(ChronoField.DAY_OF_WEEK, ChronoField.DAY_OF_WEEK.rangeRefinedBy(dateTime).getMinimum());
                break;
            default:
                return dateTime.truncatedTo(field.getTemporal().getBaseUnit());
        }
        return dateTime.truncatedTo(ChronoUnit.DAYS);
    }

    public Collection<String> getAllPatternsBetween(ZonedDateTime start, ZonedDateTime end) {
        return getAllPatternsBetween(start, end, lowestField, formatter);
    }

}
