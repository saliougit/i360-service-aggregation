// package com.innov4africa.service_aggregation.utils;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.Comparator;
// import java.util.List;
// import java.util.Map;
// import java.util.TreeMap;

// import com.innov4africa.service_aggregation.model.BalanceHistoryPoint;
// import com.innov4africa.service_aggregation.model.Period;

// public class BalanceHistoryUtils {

//     /**
//      * Fusionne les historiques iPay et iBanking
//      */
//     public static List<BalanceHistoryPoint> mergeHistories(
//             List<BalanceHistoryPoint> ipayHistory,
//             List<BalanceHistoryPoint> ibankingHistory) {
        
//         Map<LocalDate, BalanceHistoryPoint> merged = new TreeMap<>();
        
//         // Fusion iPay
//         ipayHistory.forEach(point -> 
//             merged.put(point.getRawDate().toLocalDate(), 
//                 createMergedPoint(point, point.getiPayBalance(), 0))
//         );
        
//         // Fusion iBanking
//         ibankingHistory.forEach(point -> 
//             merged.merge(point.getRawDate().toLocalDate(),
//                 createMergedPoint(point, 0, point.getiBankingBalance()),
//                 (existing, newPoint) -> combinePoints(existing, newPoint))
//         );
        
//         return new ArrayList<>(merged.values());
//     }
    
//     private static BalanceHistoryPoint createMergedPoint(BalanceHistoryPoint source, 
//                                                       double ipay, double ibanking) {
//         return new BalanceHistoryPoint(
//             source.getRawDate(),
//             ipay + ibanking,
//             ipay,
//             ibanking,
//             source.getPeriod()
//         );
//     }
    
//     private static BalanceHistoryPoint combinePoints(BalanceHistoryPoint existing, 
//                                                    BalanceHistoryPoint newPoint) {
//         return new BalanceHistoryPoint(
//             existing.getRawDate(),
//             existing.getGlobalBalance() + newPoint.getGlobalBalance(),
//             existing.getiPayBalance() + newPoint.getiPayBalance(),
//             existing.getiBankingBalance() + newPoint.getiBankingBalance(),
//             existing.getPeriod()
//         );
//     }

//     /**
//      * Complète les dates manquantes avec le dernier solde connu
//      */
//     public static List<BalanceHistoryPoint> fillMissingDates(
//             List<BalanceHistoryPoint> history,
//             Period period) {
        
//         if (history.isEmpty()) return history;
        
//         List<BalanceHistoryPoint> filled = new ArrayList<>();
//         Collections.sort(history, Comparator.comparing(BalanceHistoryPoint::getRawDate));
        
//         LocalDateTime current = period.getStartDate(history.get(0).getRawDate());
//         LocalDateTime end = period.getEndDate(history.get(history.size()-1).getRawDate());
//         int index = 0;
//         BalanceHistoryPoint lastValid = history.get(0);
        
//         while (!current.isAfter(end)) {
//             if (index < history.size() && 
//                 history.get(index).getRawDate().equals(current)) {
//                 lastValid = history.get(index);
//                 index++;
//             }
            
//             filled.add(createPointForPeriod(current, lastValid, period));
//             current = getNextDateTime(current, period);
//         }
        
//         return filled;
//     }
    
//     private static BalanceHistoryPoint createPointForPeriod(
//             LocalDateTime date, 
//             BalanceHistoryPoint source, 
//             Period period) {
//         return new BalanceHistoryPoint(
//             date,
//             source.getGlobalBalance(),
//             source.getiPayBalance(),
//             source.getiBankingBalance(),
//             period
//         );
//     }
    
//     private static LocalDateTime getNextDateTime(LocalDateTime current, Period period) {
//         return switch (period) {
//             case WEEK -> current.plusWeeks(1);
//             case MONTH -> current.plusMonths(1);
//             case YEAR -> current.plusYears(1);
//             default -> current.plusDays(1);
//         };
//     }

//     /**
//      * Filtre l'historique pour ne garder que la période courante
//      */
//     public static List<BalanceHistoryPoint> filterForCurrentPeriod(
//             List<BalanceHistoryPoint> history,
//             Period period,
//             LocalDateTime referenceDate) {
        
//         LocalDateTime start = period.getStartDate(referenceDate);
//         LocalDateTime end = period.getEndDate(referenceDate);
        
//         return history.stream()
//             .filter(point -> !point.getRawDate().isBefore(start) && 
//                             !point.getRawDate().isAfter(end))
//             .sorted(Comparator.comparing(BalanceHistoryPoint::getRawDate))
//             .toList();
//     }
// }