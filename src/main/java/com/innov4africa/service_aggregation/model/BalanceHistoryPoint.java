package com.innov4africa.service_aggregation.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.innov4africa.service_aggregation.utils.DateUtils;

public class BalanceHistoryPoint {
    @JsonIgnore
    private final LocalDateTime rawDate;
    private final double globalBalance;
    private final double iPayBalance;
    private final double iBankingBalance;
    @JsonIgnore
    private final Period period;

    public BalanceHistoryPoint(LocalDateTime date, double globalBalance, 
                             double iPayBalance, double iBankingBalance, Period period) {
        this.rawDate = date;
        this.globalBalance = DateUtils.roundToTwoDecimals(globalBalance);
        this.iPayBalance = DateUtils.roundToTwoDecimals(iPayBalance);
        this.iBankingBalance = DateUtils.roundToTwoDecimals(iBankingBalance);
        this.period = period != null ? period : Period.getDefault();
    }

    public String getDate() {
        if (period == Period.MONTH) {
            // Format spécial pour les semaines du mois : "Semaine X (dd-dd MMM)"
            int weekNumber = ((rawDate.getDayOfMonth() - 1) / 7) + 1;
            LocalDateTime weekStart = rawDate.withDayOfMonth(1).plusDays((weekNumber - 1) * 7);
            LocalDateTime weekEnd = weekStart.plusDays(6);
            if (weekEnd.isAfter(rawDate.with(TemporalAdjusters.lastDayOfMonth()))) {
                weekEnd = rawDate.with(TemporalAdjusters.lastDayOfMonth());
            }
            return String.format("Semaine %d (%d-%d %s)", 
                weekNumber,
                weekStart.getDayOfMonth(),
                weekEnd.getDayOfMonth(),
                weekEnd.format(DateTimeFormatter.ofPattern("MMM", Locale.FRENCH))
            );
        }
        return period.formatDate(rawDate);
    }

    // Getters seulement (objet immutable)
    @JsonIgnore
    public LocalDateTime getRawDate() {
        return rawDate;
    }

    public double getGlobalBalance() {
        return globalBalance;
    }

    public double getiPayBalance() {
        return iPayBalance;
    }

    public double getiBankingBalance() {
        return iBankingBalance;
    }

    @JsonIgnore
    public Period getPeriod() {
        return period;
    }
}