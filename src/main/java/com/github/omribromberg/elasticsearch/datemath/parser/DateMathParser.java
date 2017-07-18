package com.github.omribromberg.elasticsearch.datemath.parser;

import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.function.LongSupplier;


public class DateMathParser {
    private final String pattern;
    private final DateTimeZone zone;
    private final LongSupplier nowSupplier;
    private final String nowPattern;

    public DateMathParser(String pattern, DateTimeZone zone, LongSupplier nowSupplier, String nowPattern) {
        this.pattern = pattern;
        this.zone = zone;
        this.nowSupplier = nowSupplier;
        this.nowPattern = nowPattern;
    }

    public long resolveExpression(String expression) {
        return expression.startsWith(nowPattern) ? resolveNowExpression(expression) : resolveDateTimeExpression(expression);
    }

    private long parseMathExpression(String mathExpression, long time) {
        MutableDateTime dateTime = new MutableDateTime(time, zone);

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
            MutableDateTime.Property propertyToRound = null;

            switch (unit) {
                case 'y':
                    if (round) {
                        propertyToRound = dateTime.yearOfCentury();
                    } else {
                        dateTime.addYears(sign * num);
                    }
                    break;
                case 'M':
                    if (round) {
                        propertyToRound = dateTime.monthOfYear();
                    } else {
                        dateTime.addMonths(sign * num);
                    }
                    break;
                case 'w':
                    if (round) {
                        propertyToRound = dateTime.weekOfWeekyear();
                    } else {
                        dateTime.addWeeks(sign * num);
                    }
                    break;
                case 'd':
                    if (round) {
                        propertyToRound = dateTime.dayOfMonth();
                    } else {
                        dateTime.addDays(sign * num);
                    }
                    break;
                case 'h':
                case 'H':
                    if (round) {
                        propertyToRound = dateTime.hourOfDay();
                    } else {
                        dateTime.addDays(sign * num);
                    }
                    break;
                case 'm':
                    if (round) {
                        propertyToRound = dateTime.minuteOfHour();
                    } else {
                        dateTime.addMinutes(sign * num);
                    }
                    break;
                case 's':
                    if (round) {
                        propertyToRound = dateTime.secondOfMinute();
                    } else {
                        dateTime.addSeconds(sign * num);
                    }
                    break;
                default:
                    throw new DateMathParseException("unit " + unit + " not supported for date math " + mathExpression);
            }
            if (propertyToRound != null) {
                propertyToRound.roundFloor();
            }
        }
        return dateTime.getMillis();
    }

    private long parseDateTimeExpression(String dateTimeExpression) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern);
        formatter.withZone(zone);
        try {
            // we use 01/01/1970 as a base date so that things keep working with date
            // fields that are filled with times without dates
            MutableDateTime date = new MutableDateTime(1970, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC);
            final int end = formatter.parseInto(date, dateTimeExpression, 0);

            if (end < 0) {
                int position = ~end;
                throw new IllegalArgumentException("Parse failure at index [" + position + "] of [" + dateTimeExpression + "]");
            } else if (end != dateTimeExpression.length()) {
                throw new IllegalArgumentException("Unrecognized chars at the end of [" + dateTimeExpression + "]:  [" + dateTimeExpression.substring(end) + "]");
            }
            return date.getMillis();
        } catch (IllegalArgumentException e) {
            throw new DateMathParseException("failed to parse date field " + dateTimeExpression + " with format " + pattern, e);
        }
    }

    private long resolveDateTimeExpression(String dateTimeExpression) {
        int index = dateTimeExpression.indexOf("||");

        if (index == -1) {
            return parseDateTimeExpression(dateTimeExpression);
        }

        final long time = parseDateTimeExpression(dateTimeExpression.substring(0, index));
        final String mathString = dateTimeExpression.substring(index + 2);

        return parseMathExpression(mathString, time);
    }

    private long resolveNowExpression(String expression) {
        return parseMathExpression(expression.substring(nowPattern.length()), nowSupplier.getAsLong());
    }

}
