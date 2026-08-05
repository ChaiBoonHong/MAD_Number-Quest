package com.uccd3223.p1_chai_boon_hong_2206806;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HistoryRankingUtil {
    private HistoryRankingUtil() { }

    public static List<RankedHistoryRecord> rankNewestFirst(List<HistoryRecord> records) {
        Map<String, Integer> maximums = new HashMap<>();
        for (HistoryRecord record : records) {
            String category = eligibleCategory(record);
            if (category != null) maximums.merge(category, record.getScore(), Math::max);
        }
        Set<String> awarded = new HashSet<>();
        List<RankedHistoryRecord> ranked = new ArrayList<>();
        for (HistoryRecord record : records) {
            String category = eligibleCategory(record);
            boolean highest = category != null
                    && record.getScore() == maximums.get(category)
                    && awarded.add(category);
            ranked.add(new RankedHistoryRecord(record, highest));
        }
        return ranked;
    }

    static String eligibleCategory(HistoryRecord record) {
        if ("TIME_ATTACK".equals(record.getMode()) && record.getTimeLimitMs() <= 0L) return null;
        String category = ProgressManager.category(record.getMode(), record.getTimeLimitMs());
        if ("UNKNOWN".equals(category) || "TA_LEGACY".equals(category)) return null;
        return record.getGameName() + "_" + category;
    }
}
