package com.innov4africa.service_aggregation.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public enum Period {
    WEEK {
        @Override
        public String formatDate(LocalDateTime date) {
            return date.format(DateTimeFormatter.ISO_DATE); // Format: 2024-05-20
        }
    },
    MONTH {
        @Override
        public String formatDate(LocalDateTime date) {
            return "Semaine " + (date.get(java.time.temporal.WeekFields.ISO.weekOfMonth())); // Format: Semaine 1
        }
    },
    YEAR {
        @Override
        public String formatDate(LocalDateTime date) {
            return date.format(DateTimeFormatter.ofPattern("MMMM yyyy")); // Format: Janvier 2024
        }
    };

    public abstract String formatDate(LocalDateTime date);

    public static Period getDefault() {
        return WEEK;
    }

    public static Period fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return getDefault();
        }
        try {
            return Period.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return getDefault();
        }
    }
}
