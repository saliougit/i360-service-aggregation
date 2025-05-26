// package com.innov4africa.service_aggregation.model;

// import java.time.DayOfWeek;
// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.time.temporal.TemporalAdjusters;
// import java.util.ArrayList;
// import java.util.List;

// public enum Period {    WEEK {
//         @Override
//         public String formatDate(LocalDateTime date) {
//             return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // Format: 2024-05-20
//         }        @Override
//         public List<LocalDateTime> calculateDateRange(LocalDateTime currentDate) {
//             LocalDate today = currentDate.toLocalDate();
//             LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            
//             List<LocalDateTime> dates = new ArrayList<>();
//             LocalDate currentDay = monday;
//             // Ne retourner que les jours jusqu'à aujourd'hui
//             while (!currentDay.isAfter(today)) {
//                 dates.add(currentDay.atStartOfDay());
//                 currentDay = currentDay.plusDays(1);
//             }
//             return dates;
//         }
//     },
//     MONTH {
//         @Override
//         public String formatDate(LocalDateTime date) {
//             // Calculer le numéro de la semaine à partir du début du mois
//             LocalDateTime firstDayOfMonth = date.withDayOfMonth(1);
//             long weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(
//                 firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
//                 date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
//             );
//             return "Semaine " + (weeksBetween + 1); // Ajouter 1 pour commencer à la semaine 1
//         }        @Override
//         public List<LocalDateTime> calculateDateRange(LocalDateTime currentDate) {
//             LocalDate today = currentDate.toLocalDate();
//             LocalDate firstDayOfMonth = today.withDayOfMonth(1);
            
//             List<LocalDateTime> weeks = new ArrayList<>();
//             LocalDate currentWeek = firstDayOfMonth.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
//             int weekCount = 1;
            
//             // Retourner toutes les semaines du mois jusqu'à aujourd'hui
//             while (!currentWeek.isAfter(today)) {
//                 weeks.add(currentWeek.atStartOfDay());
//                 currentWeek = currentWeek.plusWeeks(1);
//             }
//             return weeks;
//         }
//     },
//     YEAR {
//         @Override
//         public String formatDate(LocalDateTime date) {
//             return date.format(DateTimeFormatter.ofPattern("MMMM yyyy")); // Format: Janvier 2024
//         }        @Override
//         public List<LocalDateTime> calculateDateRange(LocalDateTime currentDate) {
//             LocalDate today = currentDate.toLocalDate();
//             LocalDate firstDayOfYear = today.withDayOfYear(1);
            
//             List<LocalDateTime> months = new ArrayList<>();
//             LocalDate currentMonth = firstDayOfYear;
//             // Retourner tous les mois de janvier jusqu'au mois actuel
//             while (!currentMonth.isAfter(today)) {
//                 months.add(currentMonth.atStartOfDay());
//                 currentMonth = currentMonth.plusMonths(1);
//             }
//             return months;
//         }
//     };

//     public abstract String formatDate(LocalDateTime date);
//     public abstract List<LocalDateTime> calculateDateRange(LocalDateTime currentDate);

//     public static Period getDefault() {
//         return WEEK;
//     }

//     public static Period fromString(String value) {
//         if (value == null || value.trim().isEmpty()) {
//             return getDefault();
//         }
//         try {
//             return Period.valueOf(value.toUpperCase());
//         } catch (IllegalArgumentException e) {
//             return getDefault();
//         }
//     }
// }


package com.innov4africa.service_aggregation.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public enum Period {
    
    WEEK {
        @Override
        public String formatDate(LocalDateTime date) {
            return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }        @Override
        public List<LocalDateTime> calculateDateRange(LocalDateTime currentDate) {
            LocalDate today = currentDate.toLocalDate();
            
            // Utiliser WeekInfo pour avoir la vraie semaine du mois
            WeekInfo currentWeekInfo = findCurrentWeekOfMonth(today);
            
            List<LocalDateTime> dates = new ArrayList<>();
            LocalDate currentDay = currentWeekInfo.startDate;
            
            // Retourner tous les jours jusqu'à aujourd'hui
            while (!currentDay.isAfter(today)) {
                dates.add(currentDay.atStartOfDay());
                currentDay = currentDay.plusDays(1);
            }
            return dates;
        }
    },
    
    MONTH {        @Override
        public String formatDate(LocalDateTime date) {
            LocalDate dateOnly = date.toLocalDate();
            WeekInfo weekInfo = findCurrentWeekOfMonth(dateOnly);
            return String.format("Semaine %d (%d-%d %s)", 
                weekInfo.weekNumber,
                weekInfo.startDate.getDayOfMonth(),
                weekInfo.endDate.getDayOfMonth(),
                weekInfo.startDate.format(DateTimeFormatter.ofPattern("MMMM", new java.util.Locale("fr")))
            );
        }
          @Override
        public List<LocalDateTime> calculateDateRange(LocalDateTime currentDate) {
            LocalDate today = currentDate.toLocalDate();
            LocalDate firstDayOfMonth = today.withDayOfMonth(1);
            LocalDate lastDayOfMonth = firstDayOfMonth.with(TemporalAdjusters.lastDayOfMonth());
            
            List<LocalDateTime> weeks = new ArrayList<>();
            
            // Calculer toutes les semaines complètes du mois
            LocalDate currentStart = firstDayOfMonth;
            int weekNumber = 1;
            
            while (!currentStart.isAfter(today)) {
                LocalDate weekEnd = currentStart.plusDays(6);
                if (weekEnd.isAfter(lastDayOfMonth)) {
                    weekEnd = lastDayOfMonth;
                }
                if (weekEnd.isAfter(today)) {
                    weekEnd = today;
                }
                
                weeks.add(currentStart.atStartOfDay());
                currentStart = currentStart.plusDays(7);
                weekNumber++;
            }
            
            return weeks;
        }
    },
    
    YEAR {        @Override
        public String formatDate(LocalDateTime date) {
            String month = date.format(DateTimeFormatter.ofPattern("MMMM yyyy", new java.util.Locale("fr")));
            return month.substring(0, 1).toUpperCase() + month.substring(1);
        }
        
        @Override
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

    /**
     * Trouve dans quelle semaine du mois se trouve une date donnée
     */
    private static WeekInfo findCurrentWeekOfMonth(LocalDate date) {
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        DayOfWeek firstDayOfWeek = firstDayOfMonth.getDayOfWeek();
        
        // Calculer toutes les semaines du mois
        List<WeekInfo> monthWeeks = calculateMonthWeeks(firstDayOfMonth, date);
        
        // Trouver la semaine qui contient la date donnée
        for (WeekInfo week : monthWeeks) {
            if (!date.isBefore(week.startDate) && !date.isAfter(week.endDate)) {
                return week;
            }
        }
        
        // Si aucune trouvée, retourner la première semaine (cas de sécurité)
        return monthWeeks.isEmpty() ? 
            new WeekInfo(1, firstDayOfMonth, firstDayOfMonth.plusDays(6)) : 
            monthWeeks.get(0);
    }
    
    /**
     * Calcule toutes les semaines d'un mois donné jusqu'à une date limite
     */
    private static List<WeekInfo> calculateMonthWeeks(LocalDate firstDayOfMonth, LocalDate limitDate) {
        List<WeekInfo> weeks = new ArrayList<>();
        LocalDate lastDayOfMonth = firstDayOfMonth.with(TemporalAdjusters.lastDayOfMonth());
        
        // La première semaine commence le 1er du mois (quel que soit le jour)
        LocalDate weekStart = firstDayOfMonth;
        int weekNumber = 1;
        
        while (!weekStart.isAfter(limitDate) && !weekStart.isAfter(lastDayOfMonth)) {
            // Une semaine dure 7 jours, mais ne dépasse pas la fin du mois
            LocalDate weekEnd = weekStart.plusDays(6);
            if (weekEnd.isAfter(lastDayOfMonth)) {
                weekEnd = lastDayOfMonth;
            }
            
            weeks.add(new WeekInfo(weekNumber, weekStart, weekEnd));
            
            // La semaine suivante commence 7 jours après
            weekStart = weekStart.plusDays(7);
            weekNumber++;
        }
        
        return weeks;
    }
    
    /**
     * Classe interne pour représenter une semaine du mois
     */
    private static class WeekInfo {
        final int weekNumber;
        final LocalDate startDate;
        final LocalDate endDate;
        
        WeekInfo(int weekNumber, LocalDate startDate, LocalDate endDate) {
            this.weekNumber = weekNumber;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

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