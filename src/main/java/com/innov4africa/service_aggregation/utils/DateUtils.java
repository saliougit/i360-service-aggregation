package com.innov4africa.service_aggregation.utils;

public class DateUtils {
    
}
package com.innov4africa.service_aggregation.utils;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.math.RoundingMode;

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
        if (date == null || referenceDate == null || period == null) {
            return false;
        }

        LocalDateTime startDate = getStartDate(referenceDate, period);
        LocalDateTime endDate = referenceDate;

        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public static LocalDateTime getStartDate(LocalDateTime date, Period period) {
        return switch (period.getType()) {
            case WEEK -> {
                // Trouve le début de la semaine courante
                int currentDayOfMonth = date.getDayOfMonth();
                int weekNumber = (currentDayOfMonth - 1) / 7 + 1;
                int startDay = (weekNumber - 1) * 7 + 1;
                yield date.withDayOfMonth(startDay);
            }
            case MONTH -> date.withDayOfMonth(1);
            case YEAR -> date.withDayOfMonth(1).withMonth(1);
        };
    }

    public static LocalDateTime getEndDate(LocalDateTime date, Period period) {
        return switch (period.getType()) {
            case WEEK -> date;
            case MONTH -> date.withDayOfMonth(date.toLocalDate().lengthOfMonth());
            case YEAR -> date.withMonth(12).withDayOfMonth(31);
        };
    }

    public static int getWeekOfMonth(LocalDateTime date) {
        return ((date.getDayOfMonth() - 1) / 7) + 1;
    }

    public static int getMaxWeeksInMonth(LocalDateTime date) {
        return (date.toLocalDate().lengthOfMonth() - 1) / 7 + 1;
    }
    
    public static double roundToTwoDecimals(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
