package com.innov4africa.service_aggregation.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.innov4africa.service_aggregation.model.Period;

public class DateUtils {
    // Formats de date pour chaque période
    private static final Locale FRENCH = Locale.FRENCH;
    
    public static String formatWeekInMonth(LocalDateTime date) {
        int weekNumber = getWeekOfMonth(date);
        LocalDateTime weekStart = getWeekStart(date);
        LocalDateTime weekEnd = getWeekEnd(date);
        
        return String.format("Semaine %d (%d-%d %s)", 
            weekNumber,
            weekStart.getDayOfMonth(),
            weekEnd.getDayOfMonth(),
            weekEnd.format(DateTimeFormatter.ofPattern("MMM", FRENCH))
        );
    }

    public static LocalDateTime getWeekStart(LocalDateTime date) {
        int weekNumber = getWeekOfMonth(date);
        return date.withDayOfMonth(((weekNumber - 1) * 7) + 1);
    }

    public static LocalDateTime getWeekEnd(LocalDateTime date) {
        LocalDateTime weekStart = getWeekStart(date);
        LocalDateTime potentialEnd = weekStart.plusDays(6);
        
        // Si la fin potentielle dépasse la fin du mois
        if (potentialEnd.getMonth() != date.getMonth()) {
            return date.with(TemporalAdjusters.lastDayOfMonth());
        }
        
        // Si la fin potentielle dépasse la date courante
        if (potentialEnd.isAfter(date)) {
            return date;
        }
        
        return potentialEnd;
    }

    public static int getWeekOfMonth(LocalDateTime date) {
        return ((date.getDayOfMonth() - 1) / 7) + 1;
    }

    public static int getWeekCountInMonth(LocalDateTime date) {
        LocalDate lastDay = date.toLocalDate().with(TemporalAdjusters.lastDayOfMonth());
        return ((lastDay.getDayOfMonth() - 1) / 7) + 1;
    }

    public static List<LocalDateTime[]> getWeekRangesForMonth(LocalDateTime date) {
        List<LocalDateTime[]> weeks = new ArrayList<>();
        LocalDateTime firstDay = date.withDayOfMonth(1);
        int totalWeeks = getWeekCountInMonth(date);
        
        for (int weekNum = 1; weekNum <= totalWeeks; weekNum++) {
            // Calcul du début et de la fin de chaque semaine
            LocalDateTime weekStart = firstDay.plusDays((weekNum - 1) * 7);
            LocalDateTime weekEnd = weekStart.plusDays(6);
            
            // Si c'est le dernier jour du mois
            if (weekEnd.getMonth() != weekStart.getMonth()) {
                weekEnd = weekStart.with(TemporalAdjusters.lastDayOfMonth());
            }
            
            // Si on dépasse la date courante
            if (weekEnd.isAfter(date)) {
                weekEnd = date;
            }
            
            weeks.add(new LocalDateTime[]{weekStart, weekEnd});
            
            // Si on a atteint la date courante, on arrête
            if (!weekEnd.isBefore(date)) {
                break;
            }
        }
        return weeks;
    }

    public static List<LocalDateTime[]> getMonthRangesForYear(LocalDateTime date) {
        List<LocalDateTime[]> months = new ArrayList<>();
        int currentMonth = date.getMonthValue();
        
        // De janvier jusqu'au mois courant
        for (int month = 1; month <= currentMonth; month++) {
            LocalDateTime monthStart = date.withDayOfMonth(1).withMonth(month);
            LocalDateTime monthEnd;
            
            if (month == currentMonth) {
                monthEnd = date; // Pour le mois courant, utiliser la date courante
            } else {
                monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth()); // Dernier jour du mois
            }
            
            months.add(new LocalDateTime[]{monthStart, monthEnd});
        }
        return months;
    }

    public static boolean isInCurrentPeriod(LocalDateTime date, LocalDateTime currentDate, Period period) {
        LocalDateTime start = period.getStartDate(currentDate);
        LocalDateTime end = period.getEndDate(currentDate);
        return !date.isBefore(start) && !date.isAfter(end);
    }

    public static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public static List<LocalDate> getDaysInPeriod(LocalDateTime start, LocalDateTime end) {
        return start.toLocalDate().datesUntil(end.toLocalDate().plusDays(1)).toList();
    }
}