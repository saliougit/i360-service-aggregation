package com.innov4africa.service_aggregation.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.innov4africa.service_aggregation.utils.DateUtils;

public class BalanceHistoryPoint {
    @JsonIgnore
    private final LocalDateTime rawDate;
    private final double opening;
    private final double closing;
    private final String variation;
    private final ServiceBalances services;
    @JsonIgnore
    private final Period period;

    public BalanceHistoryPoint(LocalDateTime date, double opening, double closing, 
                             double ipayAmount, double ibankingAmount, Period period) {
        this.rawDate = date;
        this.opening = DateUtils.roundToTwoDecimals(opening);
        this.closing = DateUtils.roundToTwoDecimals(closing);
        this.variation = formatVariation(closing - opening);
        this.services = new ServiceBalances(
            DateUtils.roundToTwoDecimals(ipayAmount),
            DateUtils.roundToTwoDecimals(ibankingAmount)
        );
        this.period = period != null ? period : Period.getDefault();
    }

    private String formatVariation(double amount) {
        return (amount >= 0 ? "+" : "") + String.format("%.2f", amount);
    }

    public String getPeriod() {
        if (period == Period.MONTH) {
            int weekNumber = DateUtils.getWeekOfMonth(rawDate);
            LocalDateTime weekStart = rawDate.withDayOfMonth(1).plusWeeks(weekNumber - 1);
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

    // Getters
    @JsonIgnore
    public LocalDateTime getRawDate() {
        return rawDate;
    }

    @JsonProperty("period")
    public String getFormattedPeriod() {
        return getPeriod();
    }

    public double getOpening() {
        return opening;
    }

    public double getClosing() {
        return closing;
    }

    public String getVariation() {
        return variation;
    }

    public ServiceBalances getServices() {
        return services;
    }

    @JsonIgnore
    public Period getPeriodType() {
        return period;
    }
}