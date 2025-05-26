package com.innov4africa.service_aggregation.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public enum Period {    WEEK {
        @Override
        public String formatDate(LocalDateTime date) {
            return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // Format: 2024-05-20
        }        @Override
        public List<LocalDateTime> calculateDateRange(LocalDateTime currentDate) {
            LocalDate today = currentDate.toLocalDate();
            LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            
            List<LocalDateTime> dates = new ArrayList<>();
            LocalDate currentDay = monday;
            // Ne retourner que les jours jusqu'à aujourd'hui
            while (!currentDay.isAfter(today)) {
                dates.add(currentDay.atStartOfDay());
                currentDay = currentDay.plusDays(1);
            }
            return dates;
        }
    },
    MONTH {
        @Override
        public String formatDate(LocalDateTime date) {
            // Calculer le numéro de la semaine à partir du début du mois
            LocalDateTime firstDayOfMonth = date.withDayOfMonth(1);
            long weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(
                firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            );
            return "Semaine " + (weeksBetween + 1); // Ajouter 1 pour commencer à la semaine 1
        }        @Override
        public List<LocalDateTime> calculateDateRange(LocalDateTime currentDate) {
            LocalDate today = currentDate.toLocalDate();
            LocalDate firstDayOfMonth = today.withDayOfMonth(1);
            
            List<LocalDateTime> weeks = new ArrayList<>();
            LocalDate currentWeek = firstDayOfMonth.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
            int weekCount = 1;
            
            // Retourner toutes les semaines du mois jusqu'à aujourd'hui
            while (!currentWeek.isAfter(today)) {
                weeks.add(currentWeek.atStartOfDay());
                currentWeek = currentWeek.plusWeeks(1);
            }
            return weeks;
        }
    },
    YEAR {
        @Override
        public String formatDate(LocalDateTime date) {
            return date.format(DateTimeFormatter.ofPattern("MMMM yyyy")); // Format: Janvier 2024
        }        @Override
        public List<LocalDateTime> calculateDateRange(LocalDateTime currentDate) {
            LocalDate today = currentDate.toLocalDate();
            LocalDate firstDayOfYear = today.withDayOfYear(1);
            
            List<LocalDateTime> months = new ArrayList<>();
            LocalDate currentMonth = firstDayOfYear;
            // Retourner tous les mois de janvier jusqu'au mois actuel
            while (!currentMonth.isAfter(today)) {
                months.add(currentMonth.atStartOfDay());
                currentMonth = currentMonth.plusMonths(1);
            }
            return months;
        }
    };

    public abstract String formatDate(LocalDateTime date);
    public abstract List<LocalDateTime> calculateDateRange(LocalDateTime currentDate);

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
