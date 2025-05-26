package com.innov4africa.service_aggregation.utils;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import com.innov4africa.service_aggregation.model.Period;

public class DateUtils {
    private static final DateTimeFormatter WEEK_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy");
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");
    
    public static String formatDate(LocalDateTime date, Period period) {
        return switch (period) {
            case WEEK -> date.format(WEEK_FORMATTER);
            case MONTH -> date.format(MONTH_FORMATTER);
            case YEAR -> date.format(YEAR_FORMATTER);
        };
    }
    
    public static LocalDateTime[] getDateRange(LocalDateTime date, Period period) {
        LocalDateTime start, end;
        
        switch (period) {
            case WEEK:
                start = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                end = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                break;
            
            case MONTH:
                start = date.withDayOfMonth(1);
                end = date.with(TemporalAdjusters.lastDayOfMonth());
                break;
            
            case YEAR:
                start = date.withDayOfYear(1);
                end = date.with(TemporalAdjusters.lastDayOfYear());
                break;
            
            default:
                throw new IllegalArgumentException("Invalid period");
        }
        
        return new LocalDateTime[]{
            start.withHour(0).withMinute(0).withSecond(0).withNano(0),
            end.withHour(23).withMinute(59).withSecond(59).withNano(999999999)
        };
    }
    
    public static boolean isInPeriod(LocalDateTime date, LocalDateTime referenceDate, Period period) {
        LocalDateTime[] range = getDateRange(referenceDate, period);
        return !date.isBefore(range[0]) && !date.isAfter(range[1]);
    }
    
    public static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
