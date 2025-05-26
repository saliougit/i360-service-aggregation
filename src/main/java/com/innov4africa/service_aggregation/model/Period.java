package com.innov4africa.service_aggregation.model;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public enum Period {
    
    WEEK {
        @Override
        public String formatDate(LocalDateTime date) {
            return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        @Override
        public LocalDateTime getStartDate(LocalDateTime currentDate) {
            int weekNumber = ((currentDate.getDayOfMonth() - 1) / 7) + 1;
            return currentDate.withDayOfMonth(1)
                           .plusDays((weekNumber - 1) * 7)
                           .withHour(0)
                           .withMinute(0)
                           .withSecond(0);
        }
    },
    MONTH {
        @Override
        public String formatDate(LocalDateTime date) {
            return "Semaine " + ((date.getDayOfMonth() - 1) / 7 + 1);
        }

        @Override
        public LocalDateTime getStartDate(LocalDateTime currentDate) {
            return currentDate.withDayOfMonth(1)
                           .withHour(0)
                           .withMinute(0)
                           .withSecond(0);
        }
    },
    YEAR {
        @Override
        public String formatDate(LocalDateTime date) {
            return date.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH));
        }

        @Override
        public LocalDateTime getStartDate(LocalDateTime currentDate) {
            return currentDate.withDayOfYear(1)
                           .withHour(0)
                           .withMinute(0)
                           .withSecond(0);
        }
    };

    public abstract String formatDate(LocalDateTime date);
    public abstract LocalDateTime getStartDate(LocalDateTime currentDate);

    public LocalDateTime getEndDate(LocalDateTime currentDate) {
        return currentDate;
    }

    public static Period fromString(String value) {
        if (value == null) {
            return WEEK;
        }
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return WEEK;
        }
    }
}