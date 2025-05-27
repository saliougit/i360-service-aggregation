package com.innov4africa.service_aggregation.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.innov4africa.service_aggregation.utils.DateUtils;

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
            // Format de base utilisé quand non surchargé dans BalanceHistoryPoint
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

    public static Period getDefault() {
        return WEEK;
    }

     public List<LocalDateTime> calculateDateRange(LocalDateTime endDate) {
        LocalDateTime startDate;
        
        switch(this) {
            case WEEK:
                // Trouve le début de la semaine courante (ex: 22 mai pour le 27 mai)
                startDate = DateUtils.getWeekStart(endDate);
                break;
                
            case MONTH:
                // Retourne le 1er du mois courant
                startDate = endDate.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                break;
                
            case YEAR:
                // Retourne le 1er janvier de l'année courante
                startDate = endDate.withMonth(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                break;
                
            default:
                return new ArrayList<>();
        }
        
        return List.of(startDate);
    }

    public static Period fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return getDefault();
        }
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return getDefault();
        }
    }
}