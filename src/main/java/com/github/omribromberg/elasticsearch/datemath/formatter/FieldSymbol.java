package com.github.omribromberg.elasticsearch.datemath.formatter;

import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalField;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public enum FieldSymbol {
    N(ChronoField.NANO_OF_DAY, FieldSymbol::DAYS),
    n(ChronoField.NANO_OF_SECOND, FieldSymbol::SECONDS),
    A(ChronoField.MILLI_OF_DAY, FieldSymbol::DAYS),
    S(ChronoField.NANO_OF_SECOND, FieldSymbol::SECONDS),
    s(ChronoField.SECOND_OF_MINUTE, FieldSymbol::MINUTES),
    m(ChronoField.MINUTE_OF_HOUR, FieldSymbol::HOURS),
    h(ChronoField.CLOCK_HOUR_OF_AMPM, FieldSymbol::AMPM),
    K(ChronoField.HOUR_OF_AMPM, FieldSymbol::AMPM),
    k(ChronoField.CLOCK_HOUR_OF_DAY, FieldSymbol::DAYS),
    H(ChronoField.HOUR_OF_DAY, FieldSymbol::DAYS),
    a(ChronoField.AMPM_OF_DAY, FieldSymbol::DAYS),
    e(ChronoField.DAY_OF_WEEK, FieldSymbol::WEEKS),
    c(ChronoField.DAY_OF_WEEK, FieldSymbol::WEEKS),
    E(ChronoField.DAY_OF_WEEK, FieldSymbol::WEEKS),
    F(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH, FieldSymbol::WEEKS),
    d(ChronoField.DAY_OF_MONTH, FieldSymbol::MONTHS),
    D(ChronoField.DAY_OF_YEAR, FieldSymbol::YEARS),
    w(IsoFields.WEEK_OF_WEEK_BASED_YEAR, FieldSymbol::YEARS),
    W(ChronoField.ALIGNED_WEEK_OF_MONTH, FieldSymbol::MONTHS),
    L(ChronoField.MONTH_OF_YEAR, FieldSymbol::YEARS),
    M(ChronoField.MONTH_OF_YEAR, FieldSymbol::YEARS),
    q(IsoFields.QUARTER_OF_YEAR, FieldSymbol::YEARS),
    Q(IsoFields.QUARTER_OF_YEAR, FieldSymbol::YEARS),
    Y(IsoFields.WEEK_BASED_YEAR, null),
    u(ChronoField.YEAR, null),
    y(ChronoField.YEAR_OF_ERA, null),
    G(ChronoField.ERA, null);

    private static final Collection<FieldSymbol> YEARS = Arrays.asList(y, u, Y);
    private static final Collection<FieldSymbol> MONTHS = Arrays.asList(M, L);
    private static final Collection<FieldSymbol> WEEKS = Arrays.asList(W, w);
    private static final Collection<FieldSymbol> DAYS = Arrays.asList(D, d, F, E, c, e);
    private static final Collection<FieldSymbol> AMPM = Collections.singletonList(a);
    private static final Collection<FieldSymbol> HOURS = Arrays.asList(H, k, K, h);
    private static final Collection<FieldSymbol> MINUTES = Collections.singletonList(m);
    private static final Collection<FieldSymbol> SECONDS = Collections.singletonList(s);

    private final TemporalField field;
    private final Supplier<Collection<FieldSymbol>> dependenciesSuplier;

    FieldSymbol(TemporalField field, Supplier<Collection<FieldSymbol>> dependenciesSuplier) {
        this.field = field;
        this.dependenciesSuplier = dependenciesSuplier;
    }

    public static FieldSymbol of(char symbol) {
        try {
            return FieldSymbol.valueOf(String.valueOf(symbol));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Collection<FieldSymbol> YEARS() {
        return YEARS;
    }

    private static Collection<FieldSymbol> MONTHS() {
        return MONTHS;
    }

    private static Collection<FieldSymbol> WEEKS() {
        return WEEKS;
    }

    private static Collection<FieldSymbol> DAYS() {
        return DAYS;
    }

    private static Collection<FieldSymbol> AMPM() {
        return AMPM;
    }

    private static Collection<FieldSymbol> HOURS() {
        return HOURS;
    }

    private static Collection<FieldSymbol> MINUTES() {
        return MINUTES;
    }

    private static Collection<FieldSymbol> SECONDS() {
        return SECONDS;
    }

    public boolean checkDependencies(Collection<FieldSymbol> fields) {
        if (Objects.isNull(dependenciesSuplier))
            return true;

        final Collection<FieldSymbol> dependencies = dependenciesSuplier.get();

        if (Objects.isNull(dependencies))
            return true;

        final Collection<FieldSymbol> matchedDependencies = dependencies.stream()
                .filter(fields::contains)
                .collect(Collectors.toSet());

        if (matchedDependencies.isEmpty())
            return false;

        return matchedDependencies.stream()
                .filter(fieldSymbol -> fieldSymbol.checkDependencies(fields))
                .count() > 0;
    }

    public TemporalField getTemporal() {
        return field;
    }
}
