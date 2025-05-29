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
            // Pour la vue semaine, on commence au début de la semaine courante
            int weekNumber = ((currentDate.getDayOfMonth() - 1) / 7) + 1;
            return currentDate.withDayOfMonth(((weekNumber - 1) * 7) + 1)
                            .withHour(0)
                            .withMinute(0)
                            .withSecond(0);
        }
    },
    MONTH {
        @Override
        public String formatDate(LocalDateTime date) {
            return DateUtils.formatWeekInMonth(date);
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
        List<LocalDateTime> dates = new ArrayList<>();
        
        switch(this) {
            case WEEK:
                // Pour la semaine, on calcule à partir du début de la semaine courante
                int weekNumber = ((endDate.getDayOfMonth() - 1) / 7) + 1;
                LocalDateTime weekStart = endDate.withDayOfMonth(((weekNumber - 1) * 7) + 1)
                                               .withHour(0)
                                               .withMinute(0)
                                               .withSecond(0);
                
                // Ajoute chaque jour de la semaine jusqu'à la date courante
                while (!weekStart.isAfter(endDate)) {
                    dates.add(weekStart);
                    weekStart = weekStart.plusDays(1);
                }
                break;
                
            case MONTH:
                // Pour le mois, on calcule toutes les semaines jusqu'à la semaine courante
                List<LocalDateTime[]> weekRanges = DateUtils.getWeekRangesForMonth(endDate);
                for (LocalDateTime[] range : weekRanges) {
                    dates.add(range[1]); // On ajoute la fin de chaque semaine
                }
                break;
                
            case YEAR:
                // Pour l'année, on ajoute le dernier jour de chaque mois jusqu'au mois courant
                List<LocalDateTime[]> monthRanges = DateUtils.getMonthRangesForYear(endDate);
                for (LocalDateTime[] range : monthRanges) {
                    dates.add(range[1]); // On ajoute la fin de chaque mois
                }
                break;
        }
        
        return dates;
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